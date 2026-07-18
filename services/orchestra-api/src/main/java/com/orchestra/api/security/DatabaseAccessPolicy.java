package com.orchestra.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DatabaseAccessPolicy {

    private final Set<String> allowedHosts;

    public DatabaseAccessPolicy(@Value("${orchestra.data-resolver.db.allowed-hosts:}") String allowedHosts) {
        this.allowedHosts = Arrays.stream(allowedHosts.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public void validateJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:postgresql://")) {
            throw new IllegalArgumentException("Only PostgreSQL JDBC connections are supported");
        }

        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
        String host = uri.getHost();
        if (host == null || !allowedHosts.contains(host.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("JDBC host is not in the configured allowlist");
        }
    }
}
