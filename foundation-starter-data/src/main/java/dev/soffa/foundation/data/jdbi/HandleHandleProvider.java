package dev.soffa.foundation.data.jdbi;

import dev.soffa.foundation.model.TenantId;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Handle;

import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class HandleHandleProvider implements HandleProvider {

    private Handle handle;

    @Override
    public void useHandle(TenantId tenant, Consumer<Handle> consumer) {
        consumer.accept(handle);
    }

    @Override
    public <T> T withHandle(TenantId tenant, Function<Handle, T> consumer) {
        return consumer.apply(handle);
    }

    @Override
    public <T> T inTransaction(TenantId tenant, Function<Handle, T> consumer) {
        return handle.inTransaction(consumer::apply);
    }
}
