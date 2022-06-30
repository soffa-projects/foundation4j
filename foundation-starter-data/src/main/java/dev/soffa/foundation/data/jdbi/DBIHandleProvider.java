package dev.soffa.foundation.data.jdbi;

import dev.soffa.foundation.model.TenantId;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class DBIHandleProvider implements HandleProvider {

    private final Jdbi dbi;

    @Override
    public void useHandle(TenantId tenant, Consumer<Handle> consumer) {
        dbi.useHandle(consumer::accept);
    }

    @Override
    public <T> T withHandle(TenantId tenant, Function<Handle, T> consumer) {
        return dbi.withHandle(consumer::apply);
    }

    @Override
    public <T> T inTransaction(TenantId tenant, Function<Handle, T> consumer) {
        return dbi.inTransaction(consumer::apply);
    }
}
