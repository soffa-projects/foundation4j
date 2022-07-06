package dev.soffa.foundation.data.jdbi;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.common.ExtDataSource;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class DBHandleProvider implements HandleProvider {

    private final Map<String, Jdbi> cache = new ConcurrentHashMap<>();

    private final DB db;

    private Jdbi getLink(TenantId tenant) {
        String lTenant = TenantId.DEFAULT_VALUE;
        if (tenant != null) {
            lTenant = tenant.getValue();
            if (tenant.equals(TenantId.CONTEXT)) {
                lTenant = TenantHolder.get().orElse(TenantId.DEFAULT_VALUE);
            }
        }
        Preconditions.checkNotNull(lTenant, "Null tenant received while fetching database link");
        if (cache.containsKey(lTenant)) {
            return cache.get(lTenant);
        }
        Jdbi jdbi = getDataSource(lTenant);
        cache.put(lTenant, jdbi);
        return jdbi;
    }

    private Jdbi getDataSource(String lTenant) {
        ExtDataSource dataSource = (ExtDataSource) db.determineTargetDataSource(lTenant);
        Jdbi jdbi = Jdbi.create(new TransactionAwareDataSourceProxy(dataSource))
        //Jdbi jdbi = Jdbi.create(dataSource)
            .installPlugin(new SqlObjectPlugin());
        if (dataSource.isPG()) {
            jdbi.installPlugin(new PostgresPlugin());
        }
        jdbi.registerArgument(new SerializableArgumentFactory());
        jdbi.registerArgument(new Map01ArgumentFactory());
        jdbi.registerArgument(new Map02ArgumentFactory());
        jdbi.registerArgument(new Map03ArgumentFactory());
        jdbi.registerArgument(new Map04ArgumentFactory());
        jdbi.registerArgument(new List01ArgumentFactory());
        jdbi.registerArgument(new ObjectArgumentFactory());
        return jdbi;
    }

    @Override
    public void useHandle(TenantId tenant, Consumer<Handle> consumer) {
        getLink(tenant).useHandle(consumer::accept);
    }

    @Override
    public <T> T withHandle(TenantId tenant, Function<Handle, T> consumer) {
        return getLink(tenant).withHandle(consumer::apply);
    }

    @Override
    public <T> T inTransaction(TenantId tenant, Function<Handle, T> consumer) {
        return getLink(tenant).inTransaction(consumer::apply);
    }
}
