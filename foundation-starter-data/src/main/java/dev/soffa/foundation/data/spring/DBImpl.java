package dev.soffa.foundation.data.spring;

import dev.soffa.foundation.commons.CollectionUtil;
import dev.soffa.foundation.commons.EventBus;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.config.AppConfig;
import dev.soffa.foundation.data.*;
import dev.soffa.foundation.data.config.DataSourceProperties;
import dev.soffa.foundation.error.ConfigurationException;
import dev.soffa.foundation.error.InvalidTenantException;
import dev.soffa.foundation.error.NotImplementedException;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.events.DatabaseReadyEvent;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.GodClass")
public final class DBImpl extends AbstractDataSource implements ApplicationListener<ContextRefreshedEvent>, DB {

    private static final Logger LOG = Logger.get(DBImpl.class);
    private static final String TENANT_PLACEHOLDER = "__tenant__";
    private static final AtomicReference<String> LOCK = new AtomicReference<>("DB_LOCK");
    private final AppConfig appConfig;
    private final ApplicationContext context;
    private final Map<String, DatasourceInfo> registry = new ConcurrentHashMap<>();
    private String tablesPrefix;
    private String tenanstListQuery;
    private LockProvider lockProvider;


    @SneakyThrows
    public DBImpl(final ApplicationContext context,
                  final AppConfig appConfig) {

        super();
        this.context = context;
        this.appConfig = appConfig;
        if (appConfig.getDb() != null) {
            this.tenanstListQuery = appConfig.getDb().getTenantListQuery();
            this.tablesPrefix = appConfig.getDb().getTablesPrefix();
            createDatasources(appConfig.getDb().getDatasources());
            this.lockProvider = DBHelper.createLockTable(registry.get(TenantId.DEFAULT_VALUE).getDataSource(), this.tablesPrefix);
            applyMirations();
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

    private void createDatasources(Map<String, DataSourceConfig> datasources) {
        if (datasources == null || datasources.isEmpty()) {
            LOG.warn("No datasources configured for this service.");
        } else {
            for (Map.Entry<String, DataSourceConfig> dbLink : datasources.entrySet()) {
                register(dbLink.getKey(), dbLink.getValue(), false);
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
        DataSourceConfig tplConfig = registry.get(TENANT_PLACEHOLDER).getConfig();
        for (String name : names) {
            register(name, tplConfig, migrate);
        }
    }

    private void register(String id, DataSourceConfig config, boolean migrate) {
        String sourceId = id.toLowerCase();
        if (registry.containsKey(sourceId)) {
            LOG.warn("Datasource with id %s is already registered", id);
            return;
        }
        String url = config.getUrl().replace(TENANT_PLACEHOLDER, id).replace(TENANT_PLACEHOLDER.toUpperCase(), id);
        if (TENANT_PLACEHOLDER.equalsIgnoreCase(sourceId)) {
            registry.put(id.toLowerCase(), new DatasourceInfo(id, config));
        } else {
            DataSource ds = DBHelper.createDataSource(config.getName(), DataSourceProperties.create(appConfig.getName(), id, url));
            // config.setName(appConfig.getName());
            DatasourceInfo di = new DatasourceInfo(id, config, ds);
            registry.put(sourceId, di);
            if (migrate) {
                applyMigrations(id);
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
    public DataSource determineTargetDataSource(TenantId tenant) {
        Object lookupKey;
        if (tenant.equals(TenantId.CONTEXT)) {
            lookupKey = determineCurrentLookupKey();
        } else {
            lookupKey = tenant.getValue();
        }
        if (lookupKey != null) {
            lookupKey = lookupKey.toString().toLowerCase();
        }
        if (!registry.containsKey(lookupKey)) {
            throw new InvalidTenantException("%s is not a valid database link", lookupKey);
        }
        return registry.get(lookupKey).getDataSource();
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
        configureTenantsAsync();
        EventBus.post(new DatabaseReadyEvent());
    }

    @Override
    public void createSchema(String tenantId, String schema) {
        DataSource ds = registry.get(tenantId.toLowerCase()).getDataSource();
        if (ds == null) {
            throw new TechnicalException("Datasource not registered: " + tenantId);
        }
        Jdbi.create(ds).useHandle(handle -> {
            if (handle.execute("CREATE SCHEMA IF NOT EXISTS " + schema) > 0) {
                LOG.info("New schema created: %s", schema);
            }
        });
    }

    public void applyMigrations(String... datasources) {
        for (String datasource : datasources) {
            applyMigrations(datasource);
        }
    }

    public void applyMigrations(String datasource) {

        if (TENANT_PLACEHOLDER.equals(datasource)) {
            return;
        }

        String linkId = datasource.toLowerCase();
        DatasourceInfo info = registry.get(linkId);
        if (info.isMigrated()) {
            return;
        }
        synchronized (LOCK) {
            withLock("db-migration-" + linkId, 60, 30, () -> {
                String changelogPath = DBHelper.findChangeLogPath(appConfig.getName(), info.getConfig());
                if (TextUtil.isNotEmpty(changelogPath)) {
                    DBHelper.applyMigrations(info, changelogPath, tablesPrefix, appConfig.getName());
                }
                info.setMigrated(true);
                LOG.info("Migrations applied for %s", linkId);
            });
        }
    }

    @Override
    public boolean tenantExists(String tenant) {
        return registry.containsKey(tenant.toLowerCase());
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
    public DataStore newStore() {
        return new SimpleDataStore(this);
    }

    @Override
    public <E> EntityRepository<E> newEntityRepository(Class<E> entityClass) {
        return new SimpleEntityRepository<E>(this, entityClass);
    }

    public void applyMirations() {
        registry.keySet().forEach(this::applyMigrations);
    }

    @Override
    public void configureTenants() {
        DataSource defaultDs = registry.get(TenantId.DEFAULT_VALUE).getDataSource();

        if (!registry.containsKey(TENANT_PLACEHOLDER)) {
            LOG.debug("No TenantDS provided, skipping tenants migration.");
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
        } else {
            LOG.info("Loading tenants with TenantsLoader");
            try {
                TenantsLoader tenantsLoader = context.getBean(TenantsLoader.class);
                Set<String> tenantList = tenantsLoader.getTenantList();
                if (tenantList != null) {
                    tenants.addAll(tenantsLoader.getTenantList());
                }
            } catch (NoSuchBeanDefinitionException e) {
                LOG.error("No TenantsLoader defined");
            } catch (Exception e) {
                LOG.error("Error loading tenants", e);
            }
        }

        LOG.info("Tenants loaded: %d", tenants.size());
        DatasourceInfo info = registry.get(TENANT_PLACEHOLDER);
        for (String tenant : tenants) {
            register(tenant, info.getConfig(), true);
        }
        LOG.info("Database is now configured");
    }

    @Override
    public void configureTenantsAsync() {
        new Thread(this::configureTenants).start();
    }
}
