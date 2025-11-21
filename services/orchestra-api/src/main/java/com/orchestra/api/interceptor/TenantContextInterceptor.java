package com.orchestra.api.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.domain.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Base64;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantContextInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    // Default tenant for MVP/Dev mode
    private static final String DEFAULT_TENANT = "00000000-0000-0000-0000-000000000000";

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantIdStr = null;

        // 1. Try to extract from JWT (Authorization header)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String[] parts = token.split("\\.");
                if (parts.length == 3) {
                    String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                    JsonNode node = objectMapper.readTree(payload);
                    if (node.has("tenant_id")) {
                        tenantIdStr = node.get("tenant_id").asText();
                    } else if (node.has("tenantId")) {
                        tenantIdStr = node.get("tenantId").asText();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to extract tenant_id from JWT", e);
            }
        }

        // 2. Fallback to explicit header (useful for testing or dev)
        if (tenantIdStr == null) {
            tenantIdStr = request.getHeader(TENANT_HEADER);
        }

        if (tenantIdStr == null || tenantIdStr.isBlank()) {
            tenantIdStr = DEFAULT_TENANT;
        }

        try {
            TenantContext.setTenantId(UUID.fromString(tenantIdStr));
        } catch (IllegalArgumentException e) {
            // Ignore invalid UUIDs or handle error
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}

