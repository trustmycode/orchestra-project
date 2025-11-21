package com.orchestra.api.controller;

import com.orchestra.domain.dto.AiGenerateDataRequest;
import com.orchestra.domain.dto.AiGenerateDataResponse;
import com.orchestra.domain.dto.AiGenerateScenarioResponse;
import com.orchestra.domain.dto.ReportRecommendations;
import com.orchestra.domain.dto.ScenarioAnalysisRequest;
import com.orchestra.domain.dto.ScenarioAnalysisResponse;
import com.orchestra.api.service.AiService;
import com.orchestra.api.service.ScenarioAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final ScenarioAnalyzerService scenarioAnalyzerService;

    @PostMapping("/data/generate-simple")
    public ResponseEntity<Map<String, Object>> generateSimpleData() {
        Map<String, Object> generatedData = aiService.generateSimpleData();
        return ResponseEntity.ok(generatedData);
    }

    @PostMapping("/data/generate")
    public ResponseEntity<AiGenerateDataResponse> generateData(@RequestBody AiGenerateDataRequest request) {
        AiGenerateDataResponse response = aiService.generateData(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/data/generate-scenario/{scenarioId}")
    public ResponseEntity<AiGenerateScenarioResponse> generateDataForScenario(@PathVariable UUID scenarioId, @RequestParam UUID environmentId) {
        AiGenerateScenarioResponse response = aiService.generateDataForScenario(scenarioId, environmentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze-scenario")
    public ResponseEntity<ScenarioAnalysisResponse> analyzeScenario(@RequestBody ScenarioAnalysisRequest request) {
        ScenarioAnalysisResponse response = scenarioAnalyzerService.analyze(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze-report/{testRunId}")
    public ResponseEntity<ReportRecommendations> analyzeReport(@PathVariable UUID testRunId) {
        ReportRecommendations recommendations = aiService.analyzeReport(testRunId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/prompts/{key}")
    public ResponseEntity<Map<String, String>> getPrompt(@PathVariable String key) {
        String template = aiService.getPrompt(key);
        return ResponseEntity.ok(Map.of("key", key, "template", template));
    }

    @PutMapping("/prompts/{key}")
    public ResponseEntity<Void> updatePrompt(@PathVariable String key, @RequestBody Map<String, String> body) {
        aiService.updatePrompt(key, body.get("template"));
        return ResponseEntity.ok().build();
    }
}
