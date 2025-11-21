package com.orchestra.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.api.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ObjectMapper objectMapper;

    @GetMapping("/tenants")
    public ResponseEntity<List<Map<String, Object>>> getAllTenants(@RequestHeader("Authorization") String authHeader) {
        validateSuperAdmin(authHeader);
        String userId = extractUserId(authHeader);
        return ResponseEntity.ok(adminService.getAllTenants(userId));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats(@RequestHeader("Authorization") String authHeader) {
        validateSuperAdmin(authHeader);
        String userId = extractUserId(authHeader);
        return ResponseEntity.ok(adminService.getSystemStats(userId));
    }

    private void validateSuperAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        try {
            String token = authHeader.substring(7);
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid JWT format");
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode node = objectMapper.readTree(payload);
            
            // Проверка ролей (realm_access.roles или resource_access)
            boolean isSuperAdmin = false;
            if (node.has("realm_access") && node.get("realm_access").has("roles")) {
                for (JsonNode role : node.get("realm_access").get("roles")) {
                    if ("SUPER_ADMIN".equals(role.asText())) {
                        isSuperAdmin = true;
                        break;
                    }
                }
            }
            
            if (!isSuperAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: SUPER_ADMIN role required");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private String extractUserId(String authHeader) {
        try {
            String token = authHeader.substring(7);
            String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
            return objectMapper.readTree(payload).path("sub").asText("unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }
}

