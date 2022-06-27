package dev.soffa.foundation.data;

import dev.soffa.foundation.error.TodoException;
import dev.soffa.foundation.model.TenantId;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public interface DB {

    void createSchema(String linkId, String schema);

    boolean tenantExists(String tenant);

    default DataSource determineTargetDataSource() {
        return determineTargetDataSource(TenantId.CONTEXT);
    }

    default DataSource determineTargetDataSource(TenantId tenant) {
        return determineTargetDataSource(tenant.getValue());
    }

    DataSource determineTargetDataSource(String tenant);

    String getTablesPrefix();

    default Set<String> getTenantList() {
        return new HashSet<>();
    }

    default void withTenants(Consumer<String> consumer) {
        throw new TodoException();
    }

    default void withTenantsAsync(Consumer<String> consumer) {
        throw new TodoException();
    }

    default void configureTenants() {
        // Implementation not required
    }

    default void register(String[] names, boolean migrate) {
        // Implementation not required
    }


    default void withLock(String name, int atMostSeconds, int atLeastSeconds, Runnable runnable) {
        withLock(name, Duration.ofSeconds(atMostSeconds), Duration.ofSeconds(atLeastSeconds), runnable);
    }

    void withLock(String name, Duration atMost, Duration atLeast, Runnable runnable);

    default DataStore newStore() {
        throw new TodoException("Implement me");
    }

    default <E, ID> EntityRepository<E, ID> newEntityRepository(Class<E> entityClass) {
        throw new TodoException("Implement me");
    }

    DataSource getDefaultDataSource();
}
