package com.orchestra.ai.context;

import java.util.UUID;

public class AiContext {
    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();

    public static void setTenantId(UUID id) {
        TENANT_ID.set(id);
    }

    public static UUID getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}

