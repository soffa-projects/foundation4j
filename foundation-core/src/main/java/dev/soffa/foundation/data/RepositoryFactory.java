package dev.soffa.foundation.data;

import dev.soffa.foundation.model.TenantId;

public interface RepositoryFactory {

    <T> T get(Class<T> clazz, TenantId tenantId);

    default <T> T get(Class<T> clazz, String tenantId) {
        return get(clazz, TenantId.of(tenantId));
    }

    default <T> T get(Class<T> clazz) {
        return get(clazz, TenantId.CONTEXT);
    }

}
