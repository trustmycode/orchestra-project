package com.orchestra.executor.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcUrlPolicyTest {

    @Test
    void acceptsOnlyPostgresOnAllowedHosts() {
        JdbcUrlPolicy policy = new JdbcUrlPolicy("postgres,db.example.test");

        assertThatCode(() -> policy.validate("jdbc:postgresql://postgres:5432/orchestra"))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validate("jdbc:postgresql://untrusted.example/orchestra"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("allowlist");
        assertThatThrownBy(() -> policy.validate("jdbc:h2:mem:test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PostgreSQL");
    }
}
