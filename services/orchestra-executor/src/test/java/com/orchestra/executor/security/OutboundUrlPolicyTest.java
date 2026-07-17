package com.orchestra.executor.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundUrlPolicyTest {

    @Test
    void acceptsOnlyExplicitlyAllowedHosts() {
        OutboundUrlPolicy policy = new OutboundUrlPolicy("localhost", true);

        assertThatCode(() -> policy.validate("http://localhost:8080/health")).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validate("https://example.com/data"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("allowlist");
    }

    @Test
    void rejectsPrivateAddressesUnlessExplicitlyEnabled() {
        OutboundUrlPolicy policy = new OutboundUrlPolicy("localhost", false);

        assertThatThrownBy(() -> policy.validate("http://localhost/internal"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("private or local");
    }

    @Test
    void rejectsCredentialsAndUnsupportedSchemes() {
        OutboundUrlPolicy policy = new OutboundUrlPolicy("localhost", true);

        assertThatThrownBy(() -> policy.validate("file:///etc/passwd"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> policy.validate("http://user:password@localhost/data"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
