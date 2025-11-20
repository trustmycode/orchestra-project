package com.orchestra.executor.service;

import com.orchestra.domain.model.DbConnectionProfile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
    private final Map<UUID, HikariDataSource> dataSourceCache = new ConcurrentHashMap<>();

    public DataSource getDataSource(DbConnectionProfile profile) {
        return dataSourceCache.computeIfAbsent(profile.getId(), k -> createDataSource(profile));
    }

    private HikariDataSource createDataSource(DbConnectionProfile profile) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(profile.getJdbcUrl());
        config.setUsername(profile.getUsername());
        config.setPassword(secretProvider.resolve(profile.getPassword()));
        config.setMaximumPoolSize(5);
        config.setPoolName("Orchestra-DB-Pool-" + profile.getName());
        return new HikariDataSource(config);
    }
}
