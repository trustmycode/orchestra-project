package com.orchestra.ai.controller;

import com.orchestra.ai.service.PromptManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptManagerService promptManagerService;

    @GetMapping("/{key}")
    public ResponseEntity<Map<String, String>> getPrompt(@PathVariable String key) {
        try {
            String template = promptManagerService.getPrompt(key);
            return ResponseEntity.ok(Map.of("key", key, "template", template));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{key}")
    public ResponseEntity<Void> updatePrompt(@PathVariable String key, @RequestBody Map<String, String> body) {
        String template = body.get("template");
        if (template == null || template.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        promptManagerService.updatePrompt(key, template);
        return ResponseEntity.ok().build();
    }
}


