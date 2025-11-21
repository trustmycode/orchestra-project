package com.orchestra.ai.controller;

import com.orchestra.ai.agent.ReportAnalystAgent;
import com.orchestra.ai.agent.ScenarioAnalystAgent;
import com.orchestra.domain.dto.ReportAnalysisRequest;
import com.orchestra.domain.dto.ReportRecommendations;
import com.orchestra.domain.dto.ScenarioAnalysisRequest;
import com.orchestra.domain.dto.ScenarioAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AnalysisController {

    private final ReportAnalystAgent reportAnalystAgent;
    private final ScenarioAnalystAgent scenarioAnalystAgent;

    @PostMapping("/analyze-report")
    public ResponseEntity<ReportRecommendations> analyzeReport(@RequestBody ReportAnalysisRequest request) {
        return ResponseEntity.ok(reportAnalystAgent.execute(request));
    }

    @PostMapping("/analyze-scenario")
    public ResponseEntity<ScenarioAnalysisResponse> analyzeScenario(@RequestBody ScenarioAnalysisRequest request) {
        return ResponseEntity.ok(scenarioAnalystAgent.execute(request));
    }
}

