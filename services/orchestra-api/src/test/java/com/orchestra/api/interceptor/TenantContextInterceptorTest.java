package com.orchestra.api.interceptor;

import com.orchestra.domain.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TenantContextInterceptorTest {

    private static final UUID CONFIGURED_TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @AfterEach
    void clearContext() {
        TenantContext.clear();
    }

    @Test
    void ignoresClientControlledTenantHeadersAndTokens() {
        TenantContextInterceptor interceptor = new TenantContextInterceptor(CONFIGURED_TENANT.toString());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getHeader("X-Tenant-ID")).thenReturn("00000000-0000-0000-0000-000000000099");
        when(request.getHeader("Authorization")).thenReturn("Bearer untrusted.token.value");

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(TenantContext.getTenantId()).isEqualTo(CONFIGURED_TENANT);
    }
}
