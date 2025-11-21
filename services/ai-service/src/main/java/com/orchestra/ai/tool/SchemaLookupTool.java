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

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaLookupTool {

    private final ProtocolSpecRepository protocolSpecRepository;
    private final ObjectMapper objectMapper;
    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Tool(description = "Get the OpenAPI schema summary for a specific service. Useful when you need to know available endpoints and their structure.")
    @Transactional(readOnly = true)
    public String lookupSchema(
            @ToolParam(description = "Name of the service (e.g. 'order-service')") String serviceName) {

        UUID tenantId = AiContext.getTenantId();
        if (tenantId == null) {
            tenantId = DEFAULT_TENANT_ID;
        }

        log.info("Tool: Looking up schema for service '{}' and tenant {}", serviceName, tenantId);

        List<ProtocolSpec> specs = protocolSpecRepository.findByTenantIdAndServiceName(tenantId, serviceName);

        if (specs.isEmpty()) {
            log.warn("Tool: No specs found for service '{}'", serviceName);
            return "No specification found for service: " + serviceName;
        }

        // Return the latest one (assuming list order or just picking first for MVP)
        ProtocolSpec spec = specs.get(0);
        try {
            return objectMapper.writeValueAsString(spec.getParsedSummary());
        } catch (Exception e) {
            log.error("Tool: Failed to serialize spec summary", e);
            return "Error retrieving schema summary.";
        }
    }
}

