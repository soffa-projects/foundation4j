package dev.soffa.foundation.data.jdbi;

import dev.soffa.foundation.model.TenantId;
import org.jdbi.v3.core.Handle;

import java.util.function.Consumer;
import java.util.function.Function;

public interface HandleProvider {

    void useHandle(TenantId tenant, Consumer<Handle> consumer);

    <T> T withHandle(TenantId tenant, Function<Handle, T> consumer);

    <T> T inTransaction(TenantId tenant, Function<Handle, T> consumer);
}
