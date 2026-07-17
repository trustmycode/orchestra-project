package com.orchestra.api.interceptor;

import com.orchestra.domain.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class TenantContextInterceptor implements HandlerInterceptor {

    private final UUID tenantId;

    public TenantContextInterceptor(
            @Value("${orchestra.tenant-id:00000000-0000-0000-0000-000000000000}") String tenantId) {
        try {
            this.tenantId = UUID.fromString(tenantId);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("orchestra.tenant-id must be a valid UUID", exception);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        TenantContext.setTenantId(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
