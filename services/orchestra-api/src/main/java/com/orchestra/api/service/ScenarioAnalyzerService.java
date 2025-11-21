package com.orchestra.api.service;

import com.orchestra.domain.dto.ScenarioAnalysisRequest;
import com.orchestra.domain.dto.ScenarioAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioAnalyzerService {

    private final RestTemplate restTemplate;

    @Value("${orchestra.ai-service.url}")
    private String aiServiceUrl;

    public ScenarioAnalysisResponse analyze(ScenarioAnalysisRequest request) {
        log.info("Requesting scenario analysis for: {}", request.scenarioName());
        return restTemplate.postForObject(
                aiServiceUrl + "/api/v1/ai/analyze-scenario",
                request,
                ScenarioAnalysisResponse.class);
    }
}

