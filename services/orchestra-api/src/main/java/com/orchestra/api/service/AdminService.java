package com.orchestra.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    @Value("${orchestra.admin.datasource.url}")
    private String url;

    @Value("${orchestra.admin.datasource.username}")
    private String username;

    @Value("${orchestra.admin.datasource.password}")
    private String password;

    private final ObjectMapper objectMapper;
    private JdbcTemplate adminJdbcTemplate;

    @PostConstruct
    public void init() {
        log.info("Initializing Admin DataSource with user: {}", username);
        DataSource ds = DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .build();
        this.adminJdbcTemplate = new JdbcTemplate(ds);
    }

    public List<Map<String, Object>> getAllTenants(String adminUserId) {
        log.info("Admin {} requesting all tenants", adminUserId);
        audit(adminUserId, "LIST_TENANTS", "TENANT", "ALL", null);
        
        String sql = "SELECT id, name, created_at FROM tenants ORDER BY created_at DESC";
        return adminJdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> getSystemStats(String adminUserId) {
        log.info("Admin {} requesting system stats", adminUserId);
        audit(adminUserId, "VIEW_STATS", "SYSTEM", "GLOBAL", null);

        Map<String, Object> stats = new HashMap<>();
        stats.put("tenants_count", adminJdbcTemplate.queryForObject("SELECT COUNT(*) FROM tenants", Long.class));
        stats.put("users_count", adminJdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class));
        stats.put("total_runs", adminJdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_runs", Long.class));
        stats.put("active_scenarios", adminJdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_scenarios WHERE is_active = true", Long.class));
        
        return stats;
    }

    private void audit(String userId, String action, String entityType, String entityId, Map<String, Object> details) {
        try {
            String detailsJson = details != null ? objectMapper.writeValueAsString(details) : null;
            
            // Используем adminJdbcTemplate, чтобы писать в audit_logs без ограничений RLS.
            // tenant_id оставляем NULL для системных действий администратора.
            String sql = """
                INSERT INTO audit_logs (tenant_id, user_id, action, entity_type, entity_id, details, timestamp)
                VALUES (NULL, ?, ?, ?, ?, ?::jsonb, ?)
            """;
            
            adminJdbcTemplate.update(sql, 
                userId, 
                action, 
                entityType, 
                entityId, 
                detailsJson, 
                OffsetDateTime.now()
            );
        } catch (Exception e) {
            log.error("Failed to write admin audit log", e);
            // Не прерываем операцию, если аудит упал (хотя для строгого режима можно и прервать)
        }
    }
}

