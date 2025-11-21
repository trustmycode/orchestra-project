package com.orchestra.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final VectorStore vectorStore;

    public void ingest(String content, Map<String, Object> metadata) {
        log.info("Ingesting content into knowledge base. Metadata: {}", metadata);
        vectorStore.add(List.of(new Document(content, metadata)));
    }

    public List<Document> search(String query) {
        log.info("Searching knowledge base for: {}", query);
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(5)
                .build());
    }
}

