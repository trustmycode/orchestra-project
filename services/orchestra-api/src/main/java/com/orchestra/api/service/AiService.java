package com.orchestra.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;

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
}
