package dev.soffa.foundation.data.spring;

import dev.soffa.foundation.commons.*;
import dev.soffa.foundation.config.AppConfig;
import dev.soffa.foundation.data.*;
import dev.soffa.foundation.data.common.ExtDataSource;
import dev.soffa.foundation.data.migrations.Migrator;
import dev.soffa.foundation.error.*;
import dev.soffa.foundation.model.TenantId;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.multitenancy.TenantsLoader;
import lombok.SneakyThrows;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.GodClass")
public final class DBImpl extends AbstractDataSource implements ApplicationListener<ContextRefreshedEvent>, DB {

    public static final String AUTO_MIGRATE = "auto";
    private static final Logger LOG = Logger.get(DBImpl.class);
    private static final String TENANT_PLACEHOLDER = "__tenant__";
    private final AppConfig appConfig;
    private final ApplicationContext context;
    private final Map<String, ExtDataSource> registry = new ConcurrentHashMap<>();
    private final String tablesPrefix;
    private final String tenanstListQuery;
    private final LockProvider lockProvider;
    private MigrationDelegate migrationDelegate;


    @SneakyThrows
    public DBImpl(final ApplicationContext context,
                  final AppConfig appConfig) {

        super();

        this.context = context;
        this.appConfig = appConfig;
        if (appConfig.getDb() != null) {
            this.tenanstListQuery = appConfig.getDb().getTenantListQuery();
            this.tablesPrefix = appConfig.getDb().getTablesPrefix();
            createDatasources(appConfig.getDb().getTablesPrefix(), appConfig.getDb().getDatasources());
            this.lockProvider = DBHelper.createLockTable(getDefaultDataSource(), this.tablesPrefix);
            DBHelper.createPendingJobTable(getDefaultDataSource(), this.tablesPrefix);
            applyMigrations();
        } else {
            throw new TechnicalException("No database configuration found");
        }
    }

    @Override
    public String getTablesPrefix() {
        return tablesPrefix;
    }

    @Override
    public Set<String> getTenantList() {
        return registry.keySet().stream().filter(id -> {
            // EL
            return !(id.equals(TENANT_PLACEHOLDER) || id.equals(TenantId.DEFAULT_VALUE));
        }).collect(Collectors.toSet());
    }

    @Override
    public DataSource getDefaultDataSource() {
        return registry.get(TenantId.DEFAULT_VALUE);
    }

    @Override
    public void withTenants(Consumer<String> consumer) {
        Optional<String> currentTenant = TenantHolder.get();
        Set<String> tenants = getTenantList();
        tenants.forEach((id) -> {
            boolean skip = id.equals(TENANT_PLACEHOLDER) || id.equals(TenantId.DEFAULT_VALUE);
            if (!skip) {
                TenantHolder.use(id, () -> consumer.accept(id));
            }
        });
        if (currentTenant.isPresent()) {
            TenantHolder.set(currentTenant.get());
        } else {
            TenantHolder.clear();
        }
    }

    @Override
    public void withTenantsAsync(Consumer<String> consumer) {
        ExecutorService scheduler = Executors.newFixedThreadPool(registry.size());
        Set<String> tenants = getTenantList();
        tenants.forEach((id) -> {
            boolean skip = id.equals(TENANT_PLACEHOLDER) || id.equals(TenantId.DEFAULT_VALUE);
            if (!skip) {
                scheduler.execute(() -> TenantHolder.use(id, () -> consumer.accept(id)));
            }
        });
    }

    private void createDatasources(String tablesPrerix, Map<String, DataSourceConfig> datasources) {
        if (datasources == null || datasources.isEmpty()) {
            LOG.warn("No datasources configured for this service.");
        } else {
            for (Map.Entry<String, DataSourceConfig> dbLink : datasources.entrySet()) {
                // Wait for application to start before running migrations
                DataSourceConfig el = dbLink.getValue();
                el.setName(dbLink.getKey());
                el.setTablesPrefix(tablesPrerix);
                register(dbLink.getKey(), ExtDataSource.create(appConfig.getName(), el), false);
            }
            if (!registry.containsKey(TenantId.DEFAULT_VALUE)) {
                throw new TechnicalException("No default datasource provided");
            }
        }
    }

    @Override
    public void register(String[] names, boolean migrate) {
        if (!registry.containsKey(TENANT_PLACEHOLDER)) {
            throw new ConfigurationException("No tenant template (__TENANT__) provided, check your config");
        }
        ExtDataSource tplConfig = registry.get(TENANT_PLACEHOLDER);
        for (String name : names) {
            register(name, tplConfig, migrate);
        }
    }

    private void register(String id, ExtDataSource config, boolean migrate) {
        String sourceId = id.toLowerCase();
        if (registry.containsKey(sourceId)) {
            LOG.debug("Datasource with id %s is already registered", id);
            return;
        }
        if (TENANT_PLACEHOLDER.equalsIgnoreCase(sourceId)) {
            registry.put(sourceId, config);
        } else {
            ExtDataSource lconfig = config;
            if (config.isTenantTemplate()) {
                lconfig = config.ofTenant(sourceId);
            }
            if (migrate) {
                try {
                    applyMigrations(sourceId, lconfig);
                } catch (Exception e) {
                    LOG.error("Error applying migrations for datasource %s, skipping registration", id);
                    LOG.error(ErrorUtil.loookupOriginalMessage(e));
                    Sentry.get().captureException(e);
                }
            } else {
                registry.put(sourceId, lconfig);
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) {
        throw new NotImplementedException("Not supported");
    }

    @Override
    public DataSource determineTargetDataSource(String tenant) {
        Object lookupKey;
        if (tenant.equals(TenantId.CONTEXT_VALUE)) {
            lookupKey = determineCurrentLookupKey();
        } else {
            lookupKey = tenant;
        }
        if (lookupKey != null) {
            lookupKey = lookupKey.toString().toLowerCase();
        }
        if (!registry.containsKey(lookupKey)) {
            throw new InvalidTenantException("%s is not a valid database link", lookupKey);
        }
        return registry.get(lookupKey);
    }

    private Object determineCurrentLookupKey() {
        String linkId = TenantHolder.get().orElse(null);
        if (linkId == null) {
            if (registry.containsKey(TenantId.DEFAULT_VALUE)) {
                return TenantId.DEFAULT_VALUE;
            }
            throw new InvalidTenantException("Missing database link. Don't forget to set active tenant with TenantHolder.set()");
        }
        linkId = linkId.toLowerCase();
        if (!registry.containsKey(linkId) && registry.containsKey(TENANT_PLACEHOLDER)) {
            throw new InvalidTenantException("No datasource registered for tenant %s", linkId);
        }
        return linkId;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        this.configureTenants();
    }

    @Override
    public void createSchema(String tenantId, String schema) {
        DataSource ds = registry.get(tenantId.toLowerCase());
        if (ds == null) {
            throw new TechnicalException("Datasource not registered: " + tenantId);
        }
        Jdbi.create(ds).useHandle(handle -> {
            if (handle.execute("CREATE SCHEMA IF NOT EXISTS " + schema) > 0) {
                LOG.info("New schema created: %s", schema);
            }
        });
    }

    public void applyMigrations(Collection<String> datasources) {
        for (String datasource : datasources) {
            applyMigrations(datasource);
        }
    }

    public void applyMigrations(String datasource) {
        if (TENANT_PLACEHOLDER.equals(datasource)) {
            return;
        }
        ExtDataSource info = registry.get(datasource.toLowerCase());
        applyMigrations(datasource, info);
    }

    public void applyMigrations(String datasource, ExtDataSource info) {

        if (TENANT_PLACEHOLDER.equals(datasource)) {
            return;
        }

        if (info.isMigrated()) {
            return;
        }
        //withLock("db-migration-" + linkId, 60, 30, () -> {
        if (migrationDelegate == null) {
            Map<String, MigrationDelegate> beans = context.getBeansOfType(MigrationDelegate.class);
            if (beans.isEmpty()) {
                migrationDelegate = new NoMigrationDelegate();
            } else {
                migrationDelegate = beans.values().iterator().next();
            }
        }
        String lMigrationName = AUTO_MIGRATE;
        if (migrationDelegate != null && !TenantId.DEFAULT_VALUE.equals(datasource)) {
            lMigrationName = migrationDelegate.getMigrationName(datasource);
        }
        if (AUTO_MIGRATE.equalsIgnoreCase(lMigrationName)) {
            lMigrationName = info.getChangeLogPath();
        }
        String changelogPath = DBHelper.findChangeLogPath(appConfig.getName(), lMigrationName);
        if (TextUtil.isNotEmpty(changelogPath) && !info.isMigrated()) {
            if (info.isDefault()) {
                Migrator.getInstance().execute(info);
                registry.put(info.getBaseName(), info);
            } else {
                Migrator.getInstance().submit(info, out -> registry.put(out.getId(), out));
            }
        }

        //});
    }

    @Override
    public boolean tenantExists(String tenant) {
        return registry.containsKey(tenant.toLowerCase());
    }

    @Override
    public boolean isTenantReady(String tenant) {
        String id = tenant.toLowerCase();
        if (!registry.containsKey(id)) {
            Logger.platform.warn("Tenant not yet registered: %s", id);
            return false;
        }
        if (!registry.get(id).isMigrated()) {
            Logger.platform.warn("Tenant registered but not yet migrated: %s", id);
            return false;
        }
        Logger.platform.info("Tenant is now registered and migrated: %s", id);
        return true;
    }

    @Override
    public void withLock(String name, Duration atMost, Duration atLeast, Runnable runnable) {
        LockConfiguration config = new LockConfiguration(Instant.now(), name, atMost, atLeast);
        lockProvider.lock(config).ifPresent(simpleLock -> {
            try {
                runnable.run();
            } finally {
                simpleLock.unlock();
            }
        });
    }


    @Override
    public <E, ID> EntityRepository<E, ID> newEntityRepository(Class<E> entityClass) {
        return new SimpleRepository<>(this, entityClass);
    }

    public void applyMigrations() {
        this.applyMigrations(registry.keySet());
    }

    @Override
    public void configureTenants() {
        ExecutorHelper.execute(this::configureTenantsBlocking);
    }

    private void configureTenantsBlocking() {
        DataSource defaultDs = getDefaultDataSource();

        if (!registry.containsKey(TENANT_PLACEHOLDER)) {
            LOG.debug("No TenantDS provided, skipping tenants migration.");
            return;
        }

        final Set<String> tenants = new HashSet<>();
        if (TextUtil.isNotEmpty(tenanstListQuery)) {
            LOG.info("Loading tenants from database");
            Jdbi jdbi = Jdbi.create(defaultDs);
            jdbi.useHandle(handle -> {
                LOG.info("Loading tenants from query: %s", tenanstListQuery);
                List<String> results = handle.createQuery(tenanstListQuery).mapTo(String.class).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(results)) {
                    tenants.addAll(results);
                }
            });
        }

        LOG.info("Loading tenants with TenantsLoader");
        try {
            TenantsLoader tenantsLoader = context.getBean(TenantsLoader.class);
            Set<String> tenantList = tenantsLoader.getTenantList();
            if (tenantList != null && !tenantList.isEmpty()) {
                tenants.addAll(tenantList);
            }
        } catch (NoSuchBeanDefinitionException e) {
            LOG.error("No TenantsLoader defined");
        } catch (Exception e) {
            LOG.error(e, "Error loading tenants: %s", e.getMessage());
        }

        ExtDataSource info = registry.get(TENANT_PLACEHOLDER);

        boolean hasErrors = false;
        for (String tenant : tenants) {
            try {
                register(tenant, info.ofTenant(tenant), true);
            } catch (Exception e) {
                hasErrors = true;
                Logger.platform.error(e);
            }
        }
        if (hasErrors) {
            LOG.warn("Database is configured but some migrations has failed");
        } else {
            LOG.info("Database is now configured");
        }
    }

}
