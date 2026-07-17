package com.orchestra.api.controller;

import com.orchestra.api.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "orchestra.admin", name = "enabled", havingValue = "true")
public class AdminController {

    private final AdminService adminService;

    @Value("${orchestra.admin.token:}")
    private String configuredAdminToken;

    @GetMapping("/tenants")
    public ResponseEntity<List<Map<String, Object>>> getAllTenants(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        validateAdminToken(adminToken);
        return ResponseEntity.ok(adminService.getAllTenants("configured-admin"));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        validateAdminToken(adminToken);
        return ResponseEntity.ok(adminService.getSystemStats("configured-admin"));
    }

    private void validateAdminToken(String suppliedToken) {
        if (configuredAdminToken == null || configuredAdminToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Admin API is not configured");
        }
        if (suppliedToken == null || !MessageDigest.isEqual(
                configuredAdminToken.getBytes(StandardCharsets.UTF_8),
                suppliedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin token");
        }
    }
}
