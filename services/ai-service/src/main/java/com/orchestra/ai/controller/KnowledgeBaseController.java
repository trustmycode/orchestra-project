package com.orchestra.ai.controller;

import com.orchestra.ai.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ai/knowledge")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/ingest")
    public ResponseEntity<Void> ingest(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", Map.of());

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        knowledgeBaseService.ingest(content, metadata);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(@RequestParam("query") String query) {
        List<Document> results = knowledgeBaseService.search(query);
        List<Map<String, Object>> response = results.stream()
                .map(doc -> Map.of(
                        "content", doc.getText(),
                        "metadata", doc.getMetadata()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}

