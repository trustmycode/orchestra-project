package com.orchestra.executor.service;

import com.orchestra.domain.model.DbConnectionProfile;
import com.orchestra.executor.security.JdbcUrlPolicy;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ConnectionManager {

    private final SecretProvider secretProvider;
    private final JdbcUrlPolicy jdbcUrlPolicy;
    private final Map<UUID, HikariDataSource> dataSourceCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHED_POOLS = 20;

    public DataSource getDataSource(DbConnectionProfile profile) {
        if (!dataSourceCache.containsKey(profile.getId()) && dataSourceCache.size() >= MAX_CACHED_POOLS) {
            throw new IllegalStateException("Maximum number of database connection pools reached");
        }
        return dataSourceCache.computeIfAbsent(profile.getId(), k -> createDataSource(profile));
    }

    @PreDestroy
    void closeDataSources() {
        dataSourceCache.values().forEach(HikariDataSource::close);
        dataSourceCache.clear();
    }

    private HikariDataSource createDataSource(DbConnectionProfile profile) {
        jdbcUrlPolicy.validate(profile.getJdbcUrl());
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(profile.getJdbcUrl());
        config.setUsername(profile.getUsername());
        config.setPassword(secretProvider.resolve(profile.getPassword()));
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(5_000);
        config.setValidationTimeout(3_000);
        config.setLeakDetectionThreshold(30_000);
        config.setPoolName("Orchestra-DB-Pool-" + profile.getName());
        return new HikariDataSource(config);
    }
}
