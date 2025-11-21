package com.orchestra.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.domain.model.TestDataSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataIndexerService {

    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    public void index(TestDataSet dataSet) {
        if (dataSet.getData() == null || dataSet.getData().isEmpty()) {
            return;
        }

        log.info("Indexing TestDataSet: {} (ID: {})", dataSet.getName(), dataSet.getId());
        List<Document> documents = new ArrayList<>();

        // Iterate over top-level keys (e.g., "users", "orders")
        for (Map.Entry<String, Object> entry : dataSet.getData().entrySet()) {
            String entityType = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                for (Object item : list) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> record = (Map<String, Object>) item;
                        documents.add(createDocument(dataSet, entityType, record));
                    }
                }
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> record = (Map<String, Object>) value;
                documents.add(createDocument(dataSet, entityType, record));
            }
        }

        if (!documents.isEmpty()) {
            try {
                vectorStore.add(documents);
                log.info("Indexed {} documents for data set {}", documents.size(), dataSet.getId());
            } catch (Exception e) {
                log.error("Failed to index documents for data set {}", dataSet.getId(), e);
            }
        }
    }

    public void remove(UUID dataSetId) {
        log.info("Removing vectors for data set {}", dataSetId);
        // Direct cleanup via JDBC as VectorStore generic interface doesn't support delete by metadata query easily
        String sql = "DELETE FROM vector_store WHERE metadata->>'dataSetId' = ?";
        int count = jdbcTemplate.update(sql, dataSetId.toString());
        log.info("Removed {} vectors for data set {}", count, dataSetId);
    }

    @Async
    public void indexAsync(TestDataSet dataSet) {
        index(dataSet);
    }

    @Async
    public void reindexAsync(TestDataSet dataSet) {
        remove(dataSet.getId());
        index(dataSet);
    }

    @Async
    public void removeAsync(UUID dataSetId) {
        remove(dataSetId);
    }

    private Document createDocument(TestDataSet dataSet, String entityType, Map<String, Object> record) {
        String content;
        try {
            content = objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            content = record.toString();
        }

        Object recordId = record.get("id");
        String docId = generateDocId(dataSet.getId(), entityType, recordId);

        Map<String, Object> metadata = Map.of(
                "dataSetId", dataSet.getId().toString(),
                "tenantId", dataSet.getTenant().getId().toString(),
                "entityType", entityType,
                "recordId", recordId != null ? recordId.toString() : "unknown"
        );

        return new Document(docId, content, metadata);
    }

    private String generateDocId(UUID dataSetId, String entityType, Object recordId) {
        if (recordId != null) {
            return dataSetId + "_" + entityType + "_" + recordId;
        }
        return UUID.randomUUID().toString();
    }
}

