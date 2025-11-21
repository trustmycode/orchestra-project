package com.orchestra.api.service;

import com.orchestra.domain.context.TenantContext;
import com.orchestra.domain.dto.AiGenerateDataRequest;
import com.orchestra.domain.dto.AiGenerateDataResponse;
import com.orchestra.domain.dto.ReportAnalysisRequest;
import com.orchestra.domain.dto.AiGenerateScenarioResponse;
import com.orchestra.domain.dto.ReportRecommendations;
import com.orchestra.domain.dto.ScenarioAnalysisRequest;
import com.orchestra.domain.dto.ScenarioAnalysisResponse;
import com.orchestra.domain.model.TestRun;
import com.orchestra.domain.model.TestStepResult;
import com.orchestra.domain.model.TestScenario;
import com.orchestra.domain.repository.ScenarioSuiteRepository;
import com.orchestra.domain.repository.TestScenarioRepository;
import com.orchestra.domain.repository.TestRunRepository;
import com.orchestra.domain.repository.TestStepResultRepository;
import com.orchestra.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;
    private final ScenarioAnalyzerService scenarioAnalyzerService;
    private final DataResolverService dataResolverService;
    private final TestScenarioRepository testScenarioRepository;
    private final ScenarioSuiteRepository scenarioSuiteRepository;
    private final TestRunRepository testRunRepository;
    private final TestStepResultRepository testStepResultRepository;

    @Value("${orchestra.ai-service.url}")
    private String aiServiceUrl;

    /**
     * Simulates a call to an external AI service to generate simple test data.
     * In a real implementation, this would call the ai-service endpoint.
     * 
     * @return A map representing the generated JSON data.
     */
    @Deprecated
    public Map<String, Object> generateSimpleData() {
        log.info("Generating simple data via AI service (mocked)");
        // In a real scenario, you would make a REST call to the ai-service:
        // String aiServiceUrl = "http://ai-service/generate";
        // ResponseEntity<Map<String, Object>> response =
        // restTemplate.postForEntity(aiServiceUrl, request, Map.class);
        // return response.getBody();

        // For this task, we return a hardcoded map.
        return Map.of(
                "user", Map.of(
                        "id", 123,
                        "name", "John Doe",
                        "email", "john.doe@example.com"),
                "product", Map.of(
                        "id", "prod-abc-456",
                        "name", "Super Widget",
                        "price", 29.99),
                "transactionId", "txn_" + System.currentTimeMillis());
    }

    @SuppressWarnings("unchecked")
    public AiGenerateDataResponse generateData(AiGenerateDataRequest request) {
        log.info("Requesting data generation for scenario: {}, step: {}", request.getScenarioId(), request.getStepId());

        // 1. Prepare context for the Planner (ai-service)
        Map<String, Object> plannerRequest = new HashMap<>();
        plannerRequest.put("scenarioId", request.getScenarioId() != null ? request.getScenarioId().toString() : "");
        plannerRequest.put("suiteId", request.getSuiteId() != null ? request.getSuiteId().toString() : "");
        plannerRequest.put("stepId", request.getStepId() != null ? request.getStepId().toString() : "");
        plannerRequest.put("mode", request.getMode() != null ? request.getMode() : "HAPPY_PATH");
        plannerRequest.put("reasoning", "MEDIUM");
        if (request.getGlobalContext() != null) {
            plannerRequest.put("globalContext", request.getGlobalContext());
        }

        if (TenantContext.getTenantId() != null) {
            plannerRequest.put("tenantId", TenantContext.getTenantId().toString());
        }

        if (request.getScenarioId() != null) {
            testScenarioRepository.findByIdWithDetails(request.getScenarioId()).ifPresent(scenario -> {
                plannerRequest.put("scenarioName", scenario.getName());
                plannerRequest.put("tenantId", scenario.getTenant().getId().toString());
                if (request.getStepId() != null) {
                    scenario.getSteps().stream()
                            .filter(s -> s.getId().equals(request.getStepId()))
                            .findFirst()
                            .ifPresent(step -> {
                                plannerRequest.put("stepName", step.getName());
                                plannerRequest.put("channelType", step.getChannelType());
                                plannerRequest.put("action", step.getAction());
                                plannerRequest.put("endpointRef", step.getEndpointRef());
                            });
                } else {
                    List<Map<String, Object>> stepsContext = scenario.getSteps().stream()
                            .map(step -> Map.of(
                                    "stepId", step.getId().toString(),
                                    "alias", step.getAlias(),
                                    "name", step.getName(),
                                    "kind", step.getKind(),
                                    "action", step.getAction() != null ? step.getAction() : Map.of(),
                                    "endpointRef", step.getEndpointRef() != null ? step.getEndpointRef() : Map.of()))
                            .collect(Collectors.toList());
                    plannerRequest.put("steps", stepsContext);
                    log.debug("Added {} steps to planner context for scenario {}", stepsContext.size(),
                            scenario.getName());
                }
            });
        } else if (request.getSuiteId() != null) {
            scenarioSuiteRepository.findById(request.getSuiteId()).ifPresent(suite -> {
                plannerRequest.put("suiteName", suite.getName());
                plannerRequest.put("suiteDescription", suite.getDescription());
                plannerRequest.put("tenantId", suite.getTenant().getId().toString());
                plannerRequest.put("tags", suite.getTags());
            });
        }

        // 2. Call ai-service to get the Data Plan
        Map<String, Object> plannerResponse = restTemplate.postForObject(
                aiServiceUrl + "/api/v1/ai/generate",
                plannerRequest,
                Map.class);

        if (plannerResponse == null) {
            throw new RuntimeException("Received empty response from AI service");
        }

        log.debug("Received DataPlan from AI service: {}", plannerResponse);

        Map<String, Object> planCriteria = (Map<String, Object>) plannerResponse.get("result");
        String notes = (String) plannerResponse.get("notes");

        // 3. Resolve the plan into actual data using DataResolver
        Map<String, Object> resolvedData = dataResolverService.resolve(planCriteria, request.getEnvironmentId());

        log.debug("Resolved data: {}", resolvedData);

        AiGenerateDataResponse response = new AiGenerateDataResponse();
        response.setData(resolvedData);
        response.setNotes(notes);

        return response;
    }

    @SuppressWarnings("unchecked")
    public AiGenerateScenarioResponse generateDataForScenario(UUID scenarioId, UUID environmentId) {
        log.info("Starting Two-Phase Data Generation for Scenario: {}", scenarioId);

        TestScenario scenario = testScenarioRepository.findByIdWithDetails(scenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Scenario not found: " + scenarioId));

        // === Phase 1: Variable Extraction (Analysis) ===
        List<ScenarioAnalysisRequest.StepMetadata> stepMeta = scenario.getSteps().stream()
                .map(s -> new ScenarioAnalysisRequest.StepMetadata(
                        s.getAlias(),
                        s.getName(),
                        s.getKind(),
                        s.getAction() != null ? s.getAction() : Map.of()))
                .collect(Collectors.toList());

        ScenarioAnalysisRequest analysisReq = new ScenarioAnalysisRequest(
                scenario.getName(),
                "Full scenario analysis for data generation",
                stepMeta);

        ScenarioAnalysisResponse analysisResp = scenarioAnalyzerService.analyze(analysisReq);
        log.info("Phase 1 Complete. Identified {} global variables.",
                analysisResp.variables() != null ? analysisResp.variables().size() : 0);

        // === Phase 2: Global Resolution (Planner + Resolver) ===
        Map<String, Object> globalContext = new HashMap<>();
        if (analysisResp.variables() != null && !analysisResp.variables().isEmpty()) {
            Map<String, Object> plannerReq = new HashMap<>();
            plannerReq.put("mode", "GLOBAL_CONTEXT");
            plannerReq.put("tenantId", scenario.getTenant().getId().toString());
            plannerReq.put("requirements",
                    "Generate consistent values for these global variables based on the environment: "
                            + analysisResp.variables());

            // Call Planner
            Map<String, Object> plannerResponse = restTemplate.postForObject(
                    aiServiceUrl + "/api/v1/ai/generate",
                    plannerRequestHelper(plannerReq),
                    Map.class);

            if (plannerResponse != null && plannerResponse.containsKey("result")) {
                Map<String, Object> planCriteria = (Map<String, Object>) plannerResponse.get("result");
                // Call Resolver
                globalContext = dataResolverService.resolve(planCriteria, environmentId);
            }
        }
        log.info("Phase 2 Complete. Global Context: {}", globalContext);

        // === Phase 3: Step Generation (Parallel Batching) ===
        Map<String, Object> stepData = new ConcurrentHashMap<>();
        final Map<String, Object> finalGlobalContext = globalContext;

        // Use a custom ForkJoinPool to limit parallelism and avoid overloading the AI service
        ForkJoinPool customThreadPool = new ForkJoinPool(4);
        try {
            customThreadPool.submit(() ->
                scenario.getSteps().parallelStream()
                    .filter(s -> "ACTION".equals(s.getKind()))
                    .forEach(step -> {
                        AiGenerateDataRequest req = new AiGenerateDataRequest();
                        req.setScenarioId(scenarioId);
                        req.setStepId(step.getId());
                        req.setEnvironmentId(environmentId);
                        req.setGlobalContext(finalGlobalContext);

                        try {
                            AiGenerateDataResponse resp = generateData(req);
                            stepData.put(step.getAlias(), resp.getData());
                        } catch (Exception e) {
                            log.error("Failed to generate data for step {}", step.getAlias(), e);
                            stepData.put(step.getAlias(), Map.of("error", "Generation failed"));
                        }
                    })
            ).get();
        } catch (Exception e) {
            log.error("Batch generation failed", e);
            throw new RuntimeException("Batch generation failed", e);
        } finally {
            customThreadPool.shutdown();
        }

        log.info("Phase 3 Complete. Generated data for {} steps.", stepData.size());
        return new AiGenerateScenarioResponse(finalGlobalContext, stepData);
    }

    private Map<String, Object> plannerRequestHelper(Map<String, Object> base) {
        // Helper to ensure reasoning level is set
        if (!base.containsKey("reasoning")) {
            base.put("reasoning", "MEDIUM");
        }
        return base;
    }

    public ReportRecommendations analyzeReport(UUID testRunId) {
        log.info("Requesting report analysis for test run: {}", testRunId);

        TestRun testRun = testRunRepository.findById(testRunId)
                .orElseThrow(() -> new ResourceNotFoundException("TestRun not found: " + testRunId));

        List<TestStepResult> results = testStepResultRepository.findByRunIdOrderByStartedAtAsc(testRunId);

        List<ReportAnalysisRequest.FailedStepDetail> failedSteps = results.stream()
                .filter(r -> "FAILED".equals(r.getStatus()) || "FAILED_STUCK".equals(r.getStatus()))
                .map(r -> {
                    String errorMsg = "Unknown error";
                    if (r.getViolations() != null && r.getViolations().containsKey("violations")) {
                        errorMsg = r.getViolations().get("violations").toString();
                    }
                    return new ReportAnalysisRequest.FailedStepDetail(
                            r.getStepAlias(),
                            errorMsg,
                            r.getPayload());
                })
                .collect(Collectors.toList());

        ReportAnalysisRequest request = new ReportAnalysisRequest(
                testRun.getScenario().getName(),
                testRun.getStatus(),
                failedSteps);

        return restTemplate.postForObject(
                aiServiceUrl + "/api/v1/ai/analyze-report",
                request,
                ReportRecommendations.class);
    }

    @SuppressWarnings("unchecked")
    public String getPrompt(String key) {
        Map<String, String> response = restTemplate.getForObject(
                aiServiceUrl + "/api/v1/ai/prompts/" + key,
                Map.class);
        return response != null ? response.get("template") : "";
    }

    public void updatePrompt(String key, String template) {
        restTemplate.put(
                aiServiceUrl + "/api/v1/ai/prompts/" + key,
                Map.of("template", template));
    }
}
