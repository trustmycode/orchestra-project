package com.orchestra.ai.controller;

import com.orchestra.ai.context.AiContext;
import com.orchestra.ai.agent.DataTransferAgent;
import com.orchestra.ai.agent.MappingAgent;
import com.orchestra.ai.agent.ReportAnalystAgent;
import com.orchestra.ai.agent.ScenarioAnalystAgent;
import com.orchestra.ai.agent.SuiteLinkerAgent;
import com.orchestra.domain.dto.AiDataTransferRequest;
import com.orchestra.domain.dto.AiDataTransferResponse;
import com.orchestra.domain.dto.AiMappingRequest;
import com.orchestra.domain.dto.AiMappingResponse;
import com.orchestra.domain.dto.ReportAnalysisRequest;
import com.orchestra.domain.dto.ReportRecommendations;
import com.orchestra.domain.dto.ScenarioAnalysisRequest;
import com.orchestra.domain.dto.ScenarioAnalysisResponse;
import com.orchestra.domain.dto.SuiteAnalysisRequest;
import com.orchestra.domain.dto.SuiteContextPlan;
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
    private final SuiteLinkerAgent suiteLinkerAgent;
    private final MappingAgent mappingAgent;
    private final DataTransferAgent dataTransferAgent;

    @PostMapping("/analyze-report")
    public ResponseEntity<ReportRecommendations> analyzeReport(@RequestBody ReportAnalysisRequest request) {
        return ResponseEntity.ok(reportAnalystAgent.execute(request));
    }

    @PostMapping("/analyze-scenario")
    public ResponseEntity<ScenarioAnalysisResponse> analyzeScenario(@RequestBody ScenarioAnalysisRequest request) {
        return ResponseEntity.ok(scenarioAnalystAgent.execute(request));
    }

    @PostMapping("/analyze-suite")
    public ResponseEntity<SuiteContextPlan> analyzeSuite(@RequestBody SuiteAnalysisRequest request) {
        return ResponseEntity.ok(suiteLinkerAgent.execute(request));
    }

    @PostMapping("/analyze-mapping")
    public ResponseEntity<AiMappingResponse> analyzeMapping(@RequestBody AiMappingRequest request) {
        if (request.getTenantId() != null) {
            AiContext.setTenantId(request.getTenantId());
        }
        try {
            return ResponseEntity.ok(mappingAgent.execute(request));
        } finally {
            AiContext.clear();
        }
    }

    @PostMapping("/analyze-data-transfer")
    public ResponseEntity<AiDataTransferResponse> analyzeDataTransfer(@RequestBody AiDataTransferRequest request) {
        return ResponseEntity.ok(dataTransferAgent.execute(request));
    }
}

