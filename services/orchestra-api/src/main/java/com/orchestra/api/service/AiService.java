package com.orchestra.api.service;

import com.orchestra.domain.dto.AiGenerateDataRequest;
import com.orchestra.domain.dto.AiGenerateDataResponse;
import com.orchestra.domain.repository.TestScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;
    private final DataResolverService dataResolverService;
    private final TestScenarioRepository testScenarioRepository;

    @Value("${AI_SERVICE_URL:http://ai-service:8080}")
    private String aiServiceUrl;

    /**
     * Simulates a call to an external AI service to generate simple test data.
     * In a real implementation, this would call the ai-service endpoint.
     * @return A map representing the generated JSON data.
     */
    public Map<String, Object> generateSimpleData() {
        log.info("Generating simple data via AI service (mocked)");
        // In a real scenario, you would make a REST call to the ai-service:
        // String aiServiceUrl = "http://ai-service/generate";
        // ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(aiServiceUrl, request, Map.class);
        // return response.getBody();

        // For this task, we return a hardcoded map.
        return Map.of(
                "user", Map.of(
                        "id", 123,
                        "name", "John Doe",
                        "email", "john.doe@example.com"
                ),
                "product", Map.of(
                        "id", "prod-abc-456",
                        "name", "Super Widget",
                        "price", 29.99
                ),
                "transactionId", "txn_" + System.currentTimeMillis()
        );
    }

    @SuppressWarnings("unchecked")
    public AiGenerateDataResponse generateData(AiGenerateDataRequest request) {
        log.info("Requesting data generation for scenario: {}, step: {}", request.getScenarioId(), request.getStepId());

        // 1. Prepare context for the Planner (ai-service)
        Map<String, Object> plannerRequest = new HashMap<>();
        plannerRequest.put("scenarioId", request.getScenarioId() != null ? request.getScenarioId().toString() : "");
        plannerRequest.put("stepId", request.getStepId() != null ? request.getStepId().toString() : "");
        plannerRequest.put("mode", request.getMode() != null ? request.getMode() : "HAPPY_PATH");
        plannerRequest.put("reasoning", "MEDIUM");

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
                }
            });
        }

        // 2. Call ai-service to get the Data Plan
        Map<String, Object> plannerResponse = restTemplate.postForObject(
                aiServiceUrl + "/api/v1/ai/generate",
                plannerRequest,
                Map.class
        );

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
    public String getPrompt(String key) {
        Map<String, String> response = restTemplate.getForObject(
                aiServiceUrl + "/api/v1/ai/prompts/" + key,
                Map.class
        );
        return response != null ? response.get("template") : "";
    }

    public void updatePrompt(String key, String template) {
        restTemplate.put(
                aiServiceUrl + "/api/v1/ai/prompts/" + key,
                Map.of("template", template)
        );
    }
}
