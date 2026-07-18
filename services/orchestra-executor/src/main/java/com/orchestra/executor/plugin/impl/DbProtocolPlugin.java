package com.orchestra.executor.plugin.impl;

import com.orchestra.domain.model.DbConnectionProfile;
import com.orchestra.domain.model.Environment;
import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.TestRun;
import com.orchestra.domain.repository.DbConnectionProfileRepository;
import com.orchestra.domain.repository.EnvironmentRepository;
import com.orchestra.executor.model.ExecutionContext;
import com.orchestra.executor.model.StepExecutionResult;
import com.orchestra.executor.plugin.ProtocolPlugin;
import com.orchestra.executor.service.ConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbProtocolPlugin implements ProtocolPlugin {

    private static final Pattern TEMPLATE_VARIABLE = Pattern.compile("\\{\\{([A-Za-z][A-Za-z0-9_.-]*)}}");
    private static final Pattern READ_ONLY_SQL = Pattern.compile("^select\\b[\\s\\S]*", Pattern.CASE_INSENSITIVE);
    private static final int MAX_SQL_LENGTH = 10_000;

    private final ConnectionManager connectionManager;
    private final DbConnectionProfileRepository dbConnectionProfileRepository;
    private final EnvironmentRepository environmentRepository;

    @Value("${orchestra.executor.db.allow-mutations:false}")
    private boolean allowMutations;

    @Override
    public boolean supports(String channelType) {
        return "DB".equals(channelType);
    }

    @Override
    public StepExecutionResult execute(ScenarioStep step, ExecutionContext context, TestRun run) {
        log.info("Executing DB step: {} (Alias: {})", step.getName(), step.getAlias());

        Map<String, Object> actionMeta = getActionMeta(step);
        String dataSourceAlias = (String) actionMeta.get("dataSource");
        String sqlTemplate = (String) actionMeta.get("sql");

        if (dataSourceAlias == null || sqlTemplate == null) {
            throw new IllegalArgumentException("DB step requires 'dataSource' and 'sql' in action meta");
        }

        if (run.getEnvironment() == null) {
            throw new IllegalStateException("TestRun requires an Environment to execute DB steps");
        }

        UUID envId = run.getEnvironment().getId();
        Environment environment = environmentRepository.findById(envId)
                .orElseThrow(() -> new RuntimeException("Environment not found with id: " + envId));

        UUID profileId = resolveProfileId(environment, dataSourceAlias);
        DbConnectionProfile profile = dbConnectionProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("DbConnectionProfile not found: " + profileId));

        DataSource dataSource = connectionManager.getDataSource(profile);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(30);
        jdbcTemplate.setMaxRows(1000);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

        PreparedSql sql = prepareSql(sqlTemplate, context.getVariables());

        Map<String, Object> structuredOutput = new HashMap<>();
        structuredOutput.put("request", Map.of("dataSource", dataSourceAlias, "sqlRedacted", true));
        Map<String, Object> payload = new HashMap<>();

        if ("ASSERTION".equals(step.getKind())) {
            requireReadOnly(sql.statement());
            List<Map<String, Object>> results = executeAssertion(step, namedJdbcTemplate, sql);
            structuredOutput.put("response", results);
            payload.put(step.getAlias() + ".result", results);
        } else {
            if (isReadOnly(sql.statement())) {
                List<Map<String, Object>> results = namedJdbcTemplate.queryForList(sql.statement(), sql.parameters());
                payload.put(step.getAlias() + ".result", results);
                structuredOutput.put("response", results);
                log.info("DB Query executed. Rows: {}", results.size());
            } else {
                if (!allowMutations) {
                    throw new IllegalArgumentException("DB mutations are disabled by configuration");
                }
                int rows = namedJdbcTemplate.update(sql.statement(), sql.parameters());
                payload.put(step.getAlias() + ".rowsAffected", rows);
                structuredOutput.put("response", Map.of("rowsAffected", rows));
                log.info("DB Update executed. Rows affected: {}", rows);
            }
        }

        context.getVariables().putAll(payload);
        return new StepExecutionResult(structuredOutput, payload);
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

    private PreparedSql prepareSql(String template, Map<String, Object> variables) {
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("SQL must not be empty");
        }
        String normalized = template.strip();
        if (normalized.length() > MAX_SQL_LENGTH) {
            throw new IllegalArgumentException("SQL is too long");
        }
        int semicolon = normalized.indexOf(';');
        if (semicolon >= 0 && semicolon != normalized.length() - 1) {
            throw new IllegalArgumentException("Multiple SQL statements are not allowed");
        }

        Matcher matcher = TEMPLATE_VARIABLE.matcher(template);
        StringBuffer statement = new StringBuffer();
        Map<String, Object> parameters = new HashMap<>();
        int index = 0;
        while (matcher.find()) {
            String variableName = matcher.group(1);
            if (!variables.containsKey(variableName)) {
                throw new IllegalArgumentException("SQL template variable is missing: " + variableName);
            }
            Object value = variables.get(variableName);
            if (value instanceof Map<?, ?> || value instanceof Iterable<?> || (value != null && value.getClass().isArray())) {
                throw new IllegalArgumentException("SQL template variables must be scalar values");
            }
            String parameterName = "value" + index++;
            parameters.put(parameterName, value);
            matcher.appendReplacement(statement, ":" + parameterName);
        }
        matcher.appendTail(statement);
        return new PreparedSql(statement.toString(), parameters);
    }

    private boolean isReadOnly(String sql) {
        return READ_ONLY_SQL.matcher(sql.stripLeading()).matches();
    }

    private void requireReadOnly(String sql) {
        if (!isReadOnly(sql)) {
            throw new IllegalArgumentException("Assertions support only SELECT statements");
        }
    }

    private List<Map<String, Object>> executeAssertion(
            ScenarioStep step, NamedParameterJdbcTemplate jdbcTemplate, PreparedSql sql) {
        Map<String, Object> meta = getActionMeta(step);
        long timeout = getMetaLong(meta, "timeoutMs", 5000L);
        long interval = getMetaLong(meta, "pollIntervalMs", 1000L);
        long endTime = System.currentTimeMillis() + timeout;

        Throwable lastError = null;

        while (System.currentTimeMillis() < endTime) {
            try {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.statement(), sql.parameters());
                if (checkExpectations(results, step.getExpectations())) {
                    log.info("DB Assertion passed for step {}", step.getAlias());
                    return results;
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
            if (results.size() != expectedCount)
                return false;
        }

        if (rules.containsKey("minRowCount")) {
            int minCount = Integer.parseInt(rules.get("minRowCount").toString());
            if (results.size() < minCount)
                return false;
        }

        if (rules.containsKey("maxRowCount")) {
            int maxCount = Integer.parseInt(rules.get("maxRowCount").toString());
            if (results.size() > maxCount)
                return false;
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

    private record PreparedSql(String statement, Map<String, Object> parameters) {
    }
}
