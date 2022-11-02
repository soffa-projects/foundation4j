package dev.soffa.foundation.data.jdbi;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.common.ExtDataSource;
import dev.soffa.foundation.error.DatabaseException;
import dev.soffa.foundation.error.ErrorUtil;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

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
        // return JdbiUtil.create(new TransactionAwareDataSourceProxy(dataSource), dataSource.isPG());
        return JdbiUtil.create(dataSource, dataSource.isPG());
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
        int retries = 3;
        while (true) {
            try {
                return getLink(tenant).inTransaction(consumer::apply);
            } catch (Exception e) {
                String msg = ErrorUtil.loookupOriginalMessage(e);
                if (msg!=null && msg.contains("deadlock")) {
                    retries--;
                    if (retries == 0) {
                        return getLink(tenant).inTransaction(consumer::apply);
                    }
                } else {
                    throw new DatabaseException(e);
                }
            }
        }
    }
}
