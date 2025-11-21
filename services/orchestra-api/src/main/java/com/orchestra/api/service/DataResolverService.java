package com.orchestra.api.service;

import com.orchestra.api.exception.ResourceNotFoundException;
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
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    // Simple cache for DataSources to avoid recreating them for every request.
    // In a real production scenario, this should be managed more carefully (e.g.
    // with eviction).
    private final Map<UUID, DataSource> dataSourceCache = new ConcurrentHashMap<>();

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

        if (environmentId == null) {
            log.warn("No environmentId provided. Skipping SQL resolution.");
            return planCriteria;
        }

        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new RuntimeException("Environment not found: " + environmentId));

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
        String dataSourceAlias = (String) spec.get("dataSource");
        String sql = (String) spec.get("sql");
        String semanticCriteria = (String) spec.get("semanticCriteria");

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

        // RAG Logic: If semantic criteria exists, search vector store for IDs
        if (semanticCriteria != null && !semanticCriteria.isBlank()) {
            log.info("Performing semantic search for: {}", semanticCriteria);
            String tenantId = environment.getTenant().getId().toString();
            List<Document> documents = vectorStore.similaritySearch(
                    SearchRequest.builder().query(semanticCriteria).topK(5)
                            .filterExpression("tenantId == '" + tenantId + "'")
                            .build()
            );

            List<String> ids = documents.stream()
                    .map(doc -> (String) doc.getMetadata().get("recordId"))
                    .filter(id -> id != null && !id.equals("unknown"))
                    .collect(Collectors.toList());

            String idList = ids.isEmpty() ? "NULL" :
                    ids.stream().map(id -> "'" + id + "'").collect(Collectors.joining(", "));

            if (sql != null) {
                // Inject IDs into SQL placeholder {{ids}}
                sql = sql.replace("{{ids}}", idList);
            }
        } else if (sql != null && sql.contains("{{ids}}")) {
            // Fallback: SQL expects IDs but no semantic criteria provided to find them.
            // Replace with NULL to avoid SQL syntax errors (resulting in empty set).
            log.warn("SQL contains {{ids}} but no semanticCriteria provided. Replacing with NULL.");
            sql = sql.replace("{{ids}}", "NULL");
        }

        if (sql != null) {
            for (Map.Entry<String, Object> entry : spec.entrySet()) {
                if (entry.getValue() != null) {
                    String placeholder = "{{" + entry.getKey() + "}}";
                    if (sql.contains(placeholder)) {
                        sql = sql.replace(placeholder, entry.getValue().toString());
                    }
                }
            }
        }

        DataSource dataSource = dataSourceCache.computeIfAbsent(profile.getId(), k -> DataSourceBuilder.create()
                .url(profile.getJdbcUrl())
                .username(profile.getUsername())
                .password(profile.getPassword())
                .build());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        if (sql == null) {
            return null;
        }

        // If SQL implies a single row/value, we might want to simplify the result
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        if (rows.isEmpty()) {
            return null;
        } else if (rows.size() == 1) {
            return rows.get(0);
        } else {
            return rows;
        }
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
