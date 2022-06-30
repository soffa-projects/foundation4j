package dev.soffa.foundation.data.jdbi;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariDataSource;
import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class DBHandleProvider implements HandleProvider{

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
        DataSource dataSource = db.determineTargetDataSource(lTenant);
        Jdbi jdbi = Jdbi.create(new TransactionAwareDataSourceProxy(dataSource))
            .installPlugin(new SqlObjectPlugin());
        if (dataSource instanceof HikariDataSource) {
            String url = ((HikariDataSource) dataSource).getJdbcUrl();
            if (url.startsWith("jdbc:postgres")) {
                jdbi.installPlugin(new PostgresPlugin());
            }
        }
        jdbi.registerArgument(new SerializableArgumentFactory());
        jdbi.registerArgument(new MapArgumentFactory());
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
