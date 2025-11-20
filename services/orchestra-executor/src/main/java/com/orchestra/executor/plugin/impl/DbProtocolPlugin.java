package com.orchestra.executor.plugin.impl;

import com.orchestra.domain.model.DbConnectionProfile;
import com.orchestra.domain.model.Environment;
import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.TestRun;
import com.orchestra.domain.repository.DbConnectionProfileRepository;
import com.orchestra.executor.model.ExecutionContext;
import com.orchestra.executor.plugin.ProtocolPlugin;
import com.orchestra.executor.service.ConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbProtocolPlugin implements ProtocolPlugin {

    private final ConnectionManager connectionManager;
    private final DbConnectionProfileRepository dbConnectionProfileRepository;

    @Override
    public boolean supports(String channelType) {
        return "DB".equals(channelType);
    }

    @Override
    public void execute(ScenarioStep step, ExecutionContext context, TestRun run) {
        log.info("Executing DB step: {} (Alias: {})", step.getName(), step.getAlias());

        Map<String, Object> actionMeta = getActionMeta(step);
        String dataSourceAlias = (String) actionMeta.get("dataSource");
        String sqlTemplate = (String) actionMeta.get("sql");

        if (dataSourceAlias == null || sqlTemplate == null) {
            throw new IllegalArgumentException("DB step requires 'dataSource' and 'sql' in action meta");
        }

        Environment environment = run.getEnvironment();
        if (environment == null) {
            throw new IllegalStateException("TestRun requires an Environment to execute DB steps");
        }

        UUID profileId = resolveProfileId(environment, dataSourceAlias);
        DbConnectionProfile profile = dbConnectionProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("DbConnectionProfile not found: " + profileId));

        DataSource dataSource = connectionManager.getDataSource(profile);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String sql = resolveTemplate(sqlTemplate, context.getVariables());

        if ("ASSERTION".equals(step.getKind())) {
            executeAssertion(step, jdbcTemplate, sql, context);
        } else {
            if (sql.trim().toUpperCase().startsWith("SELECT")) {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
                context.getVariables().put(step.getAlias() + ".result", results);
                log.info("DB Query executed. Rows: {}", results.size());
            } else {
                int rows = jdbcTemplate.update(sql);
                context.getVariables().put(step.getAlias() + ".rowsAffected", rows);
                log.info("DB Update executed. Rows affected: {}", rows);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getActionMeta(ScenarioStep step) {
        Map<String, Object> action = step.getAction();
        if (action == null || !action.containsKey("meta")) {
            throw new IllegalArgumentException("Step action missing 'meta'");
        }
        return (Map<String, Object>) action.get("meta");
    }

    @SuppressWarnings("unchecked")
    private UUID resolveProfileId(Environment environment, String alias) {
        Map<String, Object> mappings = environment.getProfileMappings();
        if (mappings == null || !mappings.containsKey("db")) {
            throw new RuntimeException("Environment has no 'db' profile mappings");
        }
        Map<String, Object> dbMappings = (Map<String, Object>) mappings.get("db");
        Object idObj = dbMappings.get(alias);
        if (idObj == null) {
            throw new RuntimeException("No DB profile mapping found for alias: " + alias);
        }
        return UUID.fromString(idObj.toString());
    }

    private String resolveTemplate(String template, Map<String, Object> variables) {
        if (template == null || !template.contains("{{")) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            String value = String.valueOf(entry.getValue());
            result = result.replace(key, value);
        }
        return result;
    }

    private void executeAssertion(ScenarioStep step, JdbcTemplate jdbcTemplate, String sql, ExecutionContext context) {
        Map<String, Object> meta = getActionMeta(step);
        long timeout = getMetaLong(meta, "timeoutMs", 5000L);
        long interval = getMetaLong(meta, "pollIntervalMs", 1000L);
        long endTime = System.currentTimeMillis() + timeout;

        Throwable lastError = null;

        while (System.currentTimeMillis() < endTime) {
            try {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
                if (checkExpectations(results, step.getExpectations())) {
                    context.getVariables().put(step.getAlias() + ".result", results);
                    log.info("DB Assertion passed for step {}", step.getAlias());
                    return;
                }
            } catch (Exception e) {
                lastError = e;
                log.debug("DB Assertion attempt failed: {}", e.getMessage());
            }

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during DB polling", e);
            }
        }

        throw new RuntimeException("DB Assertion failed after " + timeout + "ms", lastError);
    }

    @SuppressWarnings("unchecked")
    private boolean checkExpectations(List<Map<String, Object>> results, Map<String, Object> expectations) {
        if (expectations == null || expectations.isEmpty()) {
            return true;
        }

        Map<String, Object> rules = (Map<String, Object>) expectations.get("businessRules");
        if (rules == null) {
            return true;
        }

        if (rules.containsKey("rowCount")) {
            int expectedCount = Integer.parseInt(rules.get("rowCount").toString());
            if (results.size() != expectedCount) return false;
        }

        if (rules.containsKey("minRowCount")) {
            int minCount = Integer.parseInt(rules.get("minRowCount").toString());
            if (results.size() < minCount) return false;
        }

        if (rules.containsKey("maxRowCount")) {
            int maxCount = Integer.parseInt(rules.get("maxRowCount").toString());
            if (results.size() > maxCount) return false;
        }

        if (rules.containsKey("columns")) {
            if (results.isEmpty()) {
                return false;
            }
            Map<String, Object> firstRow = results.get(0);
            Map<String, Object> expectedColumns = (Map<String, Object>) rules.get("columns");
            for (Map.Entry<String, Object> entry : expectedColumns.entrySet()) {
                Object actual = firstRow.get(entry.getKey());
                Object expected = entry.getValue();
                if (!String.valueOf(expected).equals(String.valueOf(actual))) {
                    return false;
                }
            }
        }

        return true;
    }

    private long getMetaLong(Map<String, Object> meta, String key, long defaultValue) {
        Object val = meta.get(key);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            try {
                return Long.parseLong((String) val);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
