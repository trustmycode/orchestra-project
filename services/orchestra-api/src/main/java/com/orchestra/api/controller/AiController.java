package com.orchestra.api.controller;

import com.orchestra.api.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
