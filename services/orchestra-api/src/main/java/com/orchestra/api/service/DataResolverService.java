package com.orchestra.api.service;

import com.orchestra.api.exception.ResourceNotFoundException;
import com.orchestra.api.security.DatabaseAccessPolicy;
import com.orchestra.domain.dto.DataResolverDto;
import com.orchestra.domain.mapper.DataResolverMapper;
import com.orchestra.domain.model.DataResolver;
import com.orchestra.domain.model.DbConnectionProfile;
import com.orchestra.domain.model.Environment;
import com.orchestra.domain.model.Tenant;
import com.orchestra.domain.repository.DataResolverRepository;
import com.orchestra.domain.repository.DbConnectionProfileRepository;
import com.orchestra.domain.repository.EnvironmentRepository;
import com.orchestra.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataResolverService {

    private final EnvironmentRepository environmentRepository;
    private final DbConnectionProfileRepository dbProfileRepository;
    private final DataResolverRepository dataResolverRepository;
    private final TenantRepository tenantRepository;
    private final DataResolverMapper dataResolverMapper;
    private final VectorStore vectorStore;
    private final DatabaseAccessPolicy databaseAccessPolicy;

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final Pattern SQL_PLACEHOLDER = Pattern.compile("\\{\\{([A-Za-z][A-Za-z0-9_]*)}}", Pattern.CASE_INSENSITIVE);
    private static final int MAX_SQL_LENGTH = 10_000;
    private static final int MAX_ROWS = 1_000;
    private static final int QUERY_TIMEOUT_SECONDS = 30;

    /**
     * Resolves a Data Plan (criteria) into concrete test data.
     * If the plan contains SQL instructions and an environment is provided, it
     * executes the queries.
     */
    public Map<String, Object> resolve(Map<String, Object> planCriteria, UUID environmentId) {
        log.info("Resolving data for plan criteria with environmentId: {}", environmentId);

        if (planCriteria == null) {
            return Map.of();
        }

        Environment environment = null;
        if (environmentId != null) {
            environment = environmentRepository.findById(environmentId).orElse(null);
            if (environment == null) {
                log.warn("Environment {} not found. Falling back to SYNTHETIC mode.", environmentId);
            }
        }

        if (environment == null) {
            log.info("No valid environment context. Switching to SYNTHETIC (Mock) mode.");
            return generateSyntheticData(planCriteria);
        }

        // Pre-fetch resolvers to avoid N+1 queries during recursion
        Map<String, DataResolver> resolvers = dataResolverRepository.findAllByTenantId(environment.getTenant().getId())
                .stream()
                .collect(Collectors.toMap(DataResolver::getEntityName, Function.identity()));

        Object result = resolveRecursive(planCriteria, resolvers, environment);

        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = (Map<String, Object>) result;
            return resultMap;
        }

        return planCriteria;
    }

    private Object resolveRecursive(Object value, Map<String, DataResolver> resolvers, Environment environment) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;

            // 1. Check for explicit instruction
            if (map.containsKey("dataSource") && (map.containsKey("sql") || map.containsKey("semanticCriteria"))) {
                try {
                    return executeSqlResolution(map, environment);
                } catch (Exception e) {
                    log.error("Failed to execute explicit resolution", e);
                    return null;
                }
            }

            // 2. Traverse Map
            Map<String, Object> resolvedMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();

                if (resolvers.containsKey(key)) {
                    // Resolve using configured resolver
                    DataResolver resolver = resolvers.get(key);
                    Map<String, Object> spec = new HashMap<>();
                    if (val instanceof Map) {
                        spec.putAll((Map<String, Object>) val);
                    }
                    spec.put("dataSource", resolver.getDataSource());
                    spec.put("sql", resolver.getMapping());

                    try {
                        Object resolvedVal = executeSqlResolution(spec, environment);
                        resolvedMap.put(key, resolvedVal);
                    } catch (Exception e) {
                        log.error("Failed to resolve data for key '{}' using DataResolver", key, e);
                        resolvedMap.put(key, null);
                    }
                } else {
                    // Recurse
                    resolvedMap.put(key, resolveRecursive(val, resolvers, environment));
                }
            }
            return resolvedMap;
        }

        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<Object> resolvedList = new ArrayList<>();
            for (Object item : list) {
                resolvedList.add(resolveRecursive(item, resolvers, environment));
            }
            return resolvedList;
        }

        // Primitive / Leaf
        return value;
    }

    private Object executeSqlResolution(Map<String, Object> spec, Environment environment) {
        String dataSourceAlias = requireString(spec, "dataSource");
        String sql = optionalString(spec, "sql");
        String semanticCriteria = optionalString(spec, "semanticCriteria");

        Map<String, Object> mappings = environment.getProfileMappings();
        if (mappings == null || !mappings.containsKey("db")) {
            throw new RuntimeException("No DB mappings in environment: " + environment.getName());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> dbMappings = (Map<String, Object>) mappings.get("db");
        String profileIdStr = (String) dbMappings.get(dataSourceAlias);

        if (profileIdStr == null) {
            throw new RuntimeException("No profile mapped for alias: " + dataSourceAlias);
        }

        UUID profileId = UUID.fromString(profileIdStr);
        DbConnectionProfile profile = dbProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("DbProfile not found: " + profileId));

        databaseAccessPolicy.validateJdbcUrl(profile.getJdbcUrl());
        if (sql == null) {
            return null;
        }

        Map<String, Object> parameters = new HashMap<>();

        // RAG Logic: If semantic criteria exists, search vector store for IDs
        if (semanticCriteria != null && !semanticCriteria.isBlank()) {
            log.info("Performing semantic search for tenant {}", environment.getTenant().getId());
            String tenantId = environment.getTenant().getId().toString();
            List<Document> documents = vectorStore.similaritySearch(
                    SearchRequest.builder().query(semanticCriteria).topK(5)
                            .filterExpression("tenantId == '" + tenantId + "'")
                            .build()
            );

            List<String> ids = documents.stream()
                    .map(doc -> doc.getMetadata().get("recordId"))
                    .filter(value -> value != null && !"unknown".equals(value.toString()))
                    .map(Object::toString)
                    .toList();

            parameters.put("ids", ids.isEmpty() ? List.of("__no_matching_record__") : ids);
        } else if (sql.contains("{{ids}}")) {
            parameters.put("ids", List.of("__no_matching_record__"));
        }

        Matcher matcher = SQL_PLACEHOLDER.matcher(sql);
        StringBuffer preparedSql = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);
            if (!parameters.containsKey(name)) {
                Object value = spec.get(name);
                if (value == null || value instanceof Map<?, ?> || value instanceof Iterable<?> || value.getClass().isArray()) {
                    throw new IllegalArgumentException("Missing or unsupported SQL parameter: " + name);
                }
                parameters.put(name, value);
            }
            matcher.appendReplacement(preparedSql, ":" + name);
        }
        matcher.appendTail(preparedSql);

        String validatedSql = validateReadOnlySql(preparedSql.toString());
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                profile.getJdbcUrl(), profile.getUsername(), profile.getPassword());
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        jdbcTemplate.getJdbcTemplate().setQueryTimeout(QUERY_TIMEOUT_SECONDS);
        jdbcTemplate.getJdbcTemplate().setMaxRows(MAX_ROWS);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                validatedSql, new MapSqlParameterSource(parameters));

        if (rows.isEmpty()) {
            return null;
        } else if (rows.size() == 1) {
            return rows.get(0);
        } else {
            return rows;
        }
    }

    private String validateReadOnlySql(String sql) {
        String normalized = sql == null ? "" : sql.strip();
        if (normalized.length() > MAX_SQL_LENGTH) {
            throw new IllegalArgumentException("SQL query is too long");
        }
        if (normalized.endsWith(";")) {
            normalized = normalized.substring(0, normalized.length() - 1).stripTrailing();
        }
        if (normalized.contains(";") || !normalized.toLowerCase().matches("^select\\b[\\s\\S]*")) {
            throw new IllegalArgumentException("Only a single SELECT statement is allowed");
        }
        return normalized;
    }

    private String requireString(Map<String, Object> source, String name) {
        String value = optionalString(source, name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required value: " + name);
        }
        return value;
    }

    private String optionalString(Map<String, Object> source, String name) {
        Object value = source.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String stringValue)) {
            throw new IllegalArgumentException("Value must be a string: " + name);
        }
        return stringValue;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> generateSyntheticData(Map<String, Object> criteria) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            result.put(entry.getKey(), generateSyntheticValue(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object generateSyntheticValue(String key, Object value) {
        if (value instanceof Map) {
            return generateSyntheticData((Map<String, Object>) value);
        }
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .map(item -> generateSyntheticValue(key, item))
                    .collect(Collectors.toList());
        }

        String k = key.toLowerCase();
        if (k.endsWith("id")) return UUID.randomUUID().toString();
        if (k.contains("email")) return "mock_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        if (k.contains("name")) return "Mock " + capitalize(key);
        if (k.contains("phone")) return "+15550000000";
        if (k.contains("date") || k.contains("at")) return java.time.OffsetDateTime.now().toString();
        if (k.contains("count") || k.contains("qty")) return 5;
        if (k.contains("price") || k.contains("amount")) return 99.99;
        if (k.contains("active") || k.contains("enabled")) return true;

        return "mock_" + key + "_" + UUID.randomUUID().toString().substring(0, 4);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // === CRUD Methods ===

    @Transactional(readOnly = true)
    public List<DataResolverDto> findAll() {
        return dataResolverRepository.findAll().stream()
                .map(dataResolverMapper::toDto)
                .toList();
    }

    @Transactional
    public DataResolverDto create(DataResolverDto dto) {
        DataResolver entity = dataResolverMapper.toEntity(dto);
        entity.setId(UUID.randomUUID());
        Tenant tenant = tenantRepository.findById(DEFAULT_TENANT_ID)
                .orElseThrow(() -> new IllegalStateException("Default tenant not found"));
        entity.setTenant(tenant);
        return dataResolverMapper.toDto(dataResolverRepository.save(entity));
    }

    @Transactional
    public DataResolverDto update(UUID id, DataResolverDto dto) {
        DataResolver existing = dataResolverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataResolver not found: " + id));

        existing.setEntityName(dto.getEntityName());
        existing.setDataSource(dto.getDataSource());
        existing.setMapping(dto.getMapping());

        return dataResolverMapper.toDto(dataResolverRepository.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        if (!dataResolverRepository.existsById(id)) {
            throw new ResourceNotFoundException("DataResolver not found: " + id);
        }
        dataResolverRepository.deleteById(id);
    }
}
