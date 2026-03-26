package com.orchestra.ai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.ai.context.AiContext;
import com.orchestra.domain.model.ProtocolSpec;
import com.orchestra.domain.repository.ProtocolSpecRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndpointSearchTool {

    private final ProtocolSpecRepository protocolSpecRepository;
    private final ObjectMapper objectMapper;
    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Tool(description = "Search for available API endpoints in the system. Returns a list of candidates with method, path, and summary.")
    @Transactional(readOnly = true)
    public String searchEndpoints(@ToolParam(description = "Keywords to search for (e.g. 'create order', 'get user')") String query) {
        UUID tenantId = AiContext.getTenantId();
        if (tenantId == null) {
            tenantId = DEFAULT_TENANT_ID;
        }

        log.info("Tool: Searching endpoints for query '{}' and tenant {}", query, tenantId);

        List<ProtocolSpec> specs = protocolSpecRepository.findAllByTenantId(tenantId);
        List<Map<String, String>> candidates = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (ProtocolSpec spec : specs) {
            if (spec.getParsedSummary() != null && spec.getParsedSummary().containsKey("endpoints")) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> endpoints = (List<Map<String, String>>) spec.getParsedSummary().get("endpoints");
                for (Map<String, String> ep : endpoints) {
                    String path = ep.getOrDefault("path", "").toLowerCase();
                    String summary = ep.getOrDefault("summary", "").toLowerCase();
                    String opId = ep.getOrDefault("operationId", "").toLowerCase();

                    if (path.contains(lowerQuery) || summary.contains(lowerQuery) || opId.contains(lowerQuery)) {
                        candidates.add(ep);
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            return "No endpoints found matching query: " + query;
        }

        try {
            return objectMapper.writeValueAsString(candidates);
        } catch (Exception e) {
            return "Error serializing candidates.";
        }
    }
}

