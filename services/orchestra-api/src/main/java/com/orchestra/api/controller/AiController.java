package com.orchestra.api.controller;

import com.orchestra.domain.dto.AiGenerateDataRequest;
import com.orchestra.domain.dto.AiGenerateDataResponse;
import com.orchestra.api.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

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
