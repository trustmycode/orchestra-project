package com.orchestra.api.aspect;

import com.orchestra.domain.context.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.hibernate.Session;

import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TenantAspect {

    private final EntityManager entityManager;

    @Before("execution(* com.orchestra.api.service..*(..)) && " +
            "(@within(org.springframework.transaction.annotation.Transactional) || " +
            "@annotation(org.springframework.transaction.annotation.Transactional))")
    public void setTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            // 1. Set RLS variable for PostgreSQL
            entityManager.createNativeQuery("SELECT set_config('app.current_tenant', :tenantId, true)")
                    .setParameter("tenantId", tenantId.toString())
                    .getSingleResult();

            // 2. Enable Hibernate Filter
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        }
    }
}
