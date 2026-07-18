package com.orchestra.api.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseAccessPolicyTest {

    private final DatabaseAccessPolicy policy = new DatabaseAccessPolicy("localhost,db.example.test");

    @Test
    void acceptsExplicitlyAllowedPostgresHost() {
        assertThatCode(() -> policy.validateJdbcUrl("jdbc:postgresql://db.example.test:5432/app"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsOtherHostsAndProtocols() {
        assertThatThrownBy(() -> policy.validateJdbcUrl("jdbc:postgresql://127.0.0.1:5432/app"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> policy.validateJdbcUrl("jdbc:mysql://db.example.test/app"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
