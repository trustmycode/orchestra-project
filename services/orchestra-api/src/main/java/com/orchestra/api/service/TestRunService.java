package com.orchestra.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.api.dto.TestRunCreateRequest;
import com.orchestra.api.dto.TestRunDetail;
import com.orchestra.api.exception.ResourceNotFoundException;
import com.orchestra.api.mapper.TestRunMapper;
import com.orchestra.api.model.ScenarioStep;
import com.orchestra.api.model.TestDataSet;
import com.orchestra.api.model.TestRun;
import com.orchestra.api.model.TestScenario;
import com.orchestra.api.model.TestStepResult;
import com.orchestra.api.repository.TestDataSetRepository;
import com.orchestra.api.repository.TestRunRepository;
import com.orchestra.api.repository.TestScenarioRepository;
import com.orchestra.api.repository.TestStepResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestRunService {

    private final TestRunRepository testRunRepository;
    private final TestScenarioRepository testScenarioRepository;
    private final TestStepResultRepository testStepResultRepository;
    private final TestDataSetRepository testDataSetRepository;
    private final TestRunMapper mapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TestRunDetail findRunById(UUID runId) {
        Objects.requireNonNull(runId, "runId is required");
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("TestRun not found with id: " + runId));
        List<TestStepResult> results = testStepResultRepository.findByRunIdOrderByStartedAtAsc(runId);
        return mapper.toDetail(run, results);
    }

    @Transactional
    public TestRunDetail createAndRunTest(TestRunCreateRequest request) {
        TestScenario scenario = testScenarioRepository.findByIdWithDetails(request.getScenarioId())
                .orElseThrow(() -> new ResourceNotFoundException("TestScenario not found with id: " + request.getScenarioId()));

        TestDataSet dataSet = null;
        if (request.getDataSetId() != null) {
            dataSet = testDataSetRepository.findById(request.getDataSetId())
                    .orElseThrow(() -> new ResourceNotFoundException("TestDataSet not found with id: " + request.getDataSetId()));
        }

        TestRun run = new TestRun();
        run.setId(UUID.randomUUID());
        run.setScenario(scenario);
        run.setScenarioVersion(scenario.getVersion());
        run.setMode("RUN_ALL_STEPS");
        run.setStatus("PENDING");
        run.setTenant(scenario.getTenant());
        run.setDataSet(dataSet);
        testRunRepository.save(run);

        run.setStatus("IN_PROGRESS");
        run.setStartedAt(OffsetDateTime.now());
        testRunRepository.save(run);

        boolean hasFailed = false;
        List<TestStepResult> results = new ArrayList<>();

        for (ScenarioStep step : scenario.getSteps()) {
            ScenarioStep stepToExecute = step;
            if (dataSet != null) {
                stepToExecute = substitutePlaceholders(step, dataSet.getData());
            }
            TestStepResult result = executeStep(run, stepToExecute);
            results.add(result);
            if ("FAILED".equals(result.getStatus())) {
                hasFailed = true;
            }
        }

        run.setStatus(hasFailed ? "FAILED" : "PASSED");
        run.setFinishedAt(OffsetDateTime.now());
        testRunRepository.save(run);

        return mapper.toDetail(run, results);
    }

    private TestStepResult executeStep(TestRun run, ScenarioStep step) {
        long startTime = System.currentTimeMillis();
        OffsetDateTime startedAt = OffsetDateTime.now();

        TestStepResult result = new TestStepResult();
        result.setRun(run);
        result.setStepId(step.getId());
        result.setStepAlias(step.getAlias());
        result.setStartedAt(startedAt);

        try {
            if ("HTTP_REST".equals(step.getChannelType())) {
                executeHttpStep(step, result);
            } else {
                result.setStatus("SKIPPED");
                log.warn("Skipping step '{}' of type '{}' as it is not supported.", step.getAlias(), step.getChannelType());
            }
        } catch (Exception e) {
            log.error("Error executing step '{}'", step.getAlias(), e);
            result.setStatus("FAILED");
            result.setViolations(Map.of("violations", List.of(
                    Map.of("type", "EXECUTION_ERROR", "message", e.getMessage())
            )));
        }

        result.setFinishedAt(OffsetDateTime.now());
        result.setDurationMs(System.currentTimeMillis() - startTime);
        return testStepResultRepository.save(result);
    }

    private void executeHttpStep(ScenarioStep step, TestStepResult result) {
        Map<String, Object> endpointRef = step.getEndpointRef();
        if (endpointRef == null || endpointRef.get("endpointName") == null) {
            throw new IllegalStateException("HTTP step is missing endpointRef.endpointName");
        }
        String url = (String) endpointRef.get("endpointName");
        String methodStr = "GET";
        if (step.getAction() != null && step.getAction().get("meta") instanceof Map<?, ?> meta) {
            Object methodValue = meta.get("method");
            if (methodValue instanceof String method) {
                methodStr = method;
            }
        }
        HttpMethod method = HttpMethod.valueOf(methodStr.toUpperCase());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, method, requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            HttpHeaders errorHeaders = e.getResponseHeaders() != null ? e.getResponseHeaders() : new HttpHeaders();
            response = new ResponseEntity<>(e.getResponseBodyAsString(), errorHeaders, e.getStatusCode());
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            result.setPayload(Map.of("statusCode", response.getStatusCode().value(), "body", payload, "headers", response.getHeaders()));
        } catch (Exception e) {
            result.setPayload(Map.of("statusCode", response.getStatusCode().value(), "body", response.getBody(), "headers", response.getHeaders()));
        }

        List<Map<String, Object>> violations = new ArrayList<>();
        if (step.getExpectations() != null && step.getExpectations().get("expectedStatusCode") != null) {
            int expectedStatus = (Integer) step.getExpectations().get("expectedStatusCode");
            if (response.getStatusCode().value() != expectedStatus) {
                violations.add(Map.of("type", "STATUS_CODE_MISMATCH", "message", "Expected status " + expectedStatus + " but got " + response.getStatusCode().value()));
            }
        }

        result.setStatus(violations.isEmpty() && response.getStatusCode().is2xxSuccessful() ? "PASSED" : "FAILED");
        if (!violations.isEmpty()) {
            result.setViolations(Map.of("violations", violations));
        }
    }

    private ScenarioStep substitutePlaceholders(ScenarioStep originalStep, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return originalStep;
        }
        try {
            String stepJson = objectMapper.writeValueAsString(originalStep);
            String substitutedJson = replacePlaceholders(stepJson, data);
            return objectMapper.readValue(substitutedJson, ScenarioStep.class);
        } catch (IOException e) {
            log.error("Failed to substitute placeholders for step {}", originalStep.getAlias(), e);
            return originalStep;
        }
    }

    private String replacePlaceholders(String json, Map<String, Object> data) {
        Pattern pattern = Pattern.compile("\\{\\{data\\.([\\w.-]+)\\}\\}");
        Matcher matcher = pattern.matcher(json);

        return matcher.replaceAll(matchResult -> {
            String path = matchResult.group(1);
            Object value = getValueFromPath(data, path);
            if (value != null) {
                return Matcher.quoteReplacement(String.valueOf(value));
            } else {
                return matchResult.group(0);
            }
        });
    }

    private Object getValueFromPath(Map<String, Object> data, String path) {
        String[] keys = path.split("\\.");
        Object currentValue = data;
        for (String key : keys) {
            if (currentValue instanceof Map) {
                currentValue = ((Map<?, ?>) currentValue).get(key);
                if (currentValue == null) {
                    return null;
                }
            } else {
                return null;
            }
        }
        return currentValue;
    }
}
