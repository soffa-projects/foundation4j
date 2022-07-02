package dev.soffa.foundation.data.migrations;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.data.common.ExtDataSource;
import dev.soffa.foundation.error.DatabaseException;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.model.TenantId;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Migrator implements Observer<MigrationJob> {

    private static final ResourceLoader RL = new DefaultResourceLoader();
    private final PublishSubject<MigrationJob> subject = PublishSubject.create();
    private final Queue<String> pendingJobs = new java.util.concurrent.LinkedBlockingQueue<>();

    private static final Migrator INSTANCE = new Migrator();

    private final AtomicInteger counter = new AtomicInteger(0);


    public Migrator() {
        subject.subscribe(this);
    }

    public void submit(ExtDataSource job) {
        synchronized (counter) {
            counter.incrementAndGet();
            subject.onNext(new MigrationJob(job, null));
        }
    }

    public void execute(ExtDataSource job) {
        synchronized (counter) {
            if (pendingJobs.contains(job.getName())) {
                Logger.platform.warn("Migration job already submitted: %s",  job.getName());
                return;
            }
            counter.incrementAndGet();
            pendingJobs.add(job.getName());
            applyMigrations(job);
            job.setMigrated(true);
            counter.decrementAndGet();
        }
    }

    public void submit(ExtDataSource job, Consumer<ExtDataSource> callback) {
        synchronized(counter) {
            if (pendingJobs.contains(job.getName())) {
                Logger.platform.warn("Migration job already submitted: %s",  job.getName());
                return;
            }
            counter.incrementAndGet();
            pendingJobs.add(job.getName());
            subject.onNext(new MigrationJob(job, callback));
        }
    }

    public static Migrator getInstance() {
        return INSTANCE;
    }

    public int getCounter() {
        return counter.get();
    }

    public boolean isEmpty() {
        return counter.get() == 0;
    }

    private void applyMigrations(ExtDataSource ds) {

        String name = ds.getName();
        String changeLogPath = ds.getChangeLogPath();
        String tablesPrefix = ds.getTablesPrefix();
        String appicationName = ds.getApplicationName();

        Preconditions.checkNotNull(ds, "DataSource cannot be null");
        Preconditions.checkNotNull(name, "name is null");
        Preconditions.checkNotNull(changeLogPath, "changeLogPath cannot be null");
        Preconditions.checkNotNull(appicationName, "application name is required");

        SpringLiquibase lqb = new SpringLiquibase();
        lqb.setDropFirst(false);
        lqb.setResourceLoader(RL);
        Map<String, String> changeLogParams = new HashMap<>();

        changeLogParams.put("prefix", "");
        changeLogParams.put("table_prefix", "");
        changeLogParams.put("tables_prefix", "");
        changeLogParams.put("tablePrefix", "");
        changeLogParams.put("tablesPrefix", "");


        if (TextUtil.isNotEmpty(tablesPrefix)) {
            changeLogParams.put("prefix", tablesPrefix);
            changeLogParams.put("table_prefix", tablesPrefix);
            changeLogParams.put("tables_prefix", tablesPrefix);
            changeLogParams.put("tablePrefix", tablesPrefix);
            changeLogParams.put("tablesPrefix", tablesPrefix);

            lqb.setDatabaseChangeLogLockTable(tablesPrefix + "changelog_lock");
            lqb.setDatabaseChangeLogTable(tablesPrefix + "changelog");
        }
        if (TextUtil.isNotEmpty(appicationName)) {
            changeLogParams.put("application", appicationName);
            changeLogParams.put("applicationName", appicationName);
            changeLogParams.put("application_name", appicationName);
        }

        Resource res = RL.getResource(changeLogPath);
        if (!res.exists()) {
            List<String> lookup = new ArrayList<>();

            lookup.add("classpath:/db/changelog/" + changeLogPath + ".xml");
            lookup.add("classpath:/db/changelog/" + appicationName + "/" + changeLogPath + ".xml");

            boolean found= false;
            for (String candidate : lookup) {
                res = RL.getResource(candidate);
                if (res.exists()) {
                    changeLogPath = candidate;
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new TechnicalException("Liquibase changeLog was not found: %s", changeLogPath);
            }
        }
        lqb.setChangeLog(changeLogPath);
        doApplyMigration(ds, name, lqb, changeLogParams);
    }

    private void doApplyMigration(ExtDataSource ds, String name, SpringLiquibase lqb, Map<String, String> changeLogParams) {
        @SuppressWarnings("PMD.CloseResource")
        String schema = ds.getSchema();
        if (TenantId.DEFAULT_VALUE.equals(name)) {
            lqb.setContexts(TenantId.DEFAULT_VALUE);
        } else {
            lqb.setContexts("tenant," + name);
        }
        if (TextUtil.isNotEmpty(schema)) {
            lqb.setDefaultSchema(schema);
            lqb.setLiquibaseSchema(schema);
        }
        lqb.setChangeLogParameters(changeLogParams);
        try {
            lqb.setDataSource(ds);
            lqb.afterPropertiesSet(); // Run migrations
            Logger.app.info("[datasource:%s] migration '%s' successfully applied", name, lqb.getChangeLog());
        } catch (Exception e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("changelog") && msg.contains("already exists")) {
                boolean isTestDb = ds.isH2();
                if (!isTestDb) {
                    Logger.app.warn("Looks like migrations are being ran twice for %s.%s, ignore this error", name, schema);
                }
            } else {
                throw new DatabaseException(e, "Migration failed for %s", schema);
            }
        }
    }


    @Override
    public void onSubscribe(@NonNull Disposable d) {
        // Nothing to do
    }

    @Override
    public void onNext(@NonNull MigrationJob job) {
        Logger.platform.info("Migration: %s", job.getInfo().getName());
        try {
            applyMigrations(job.getInfo());
        }catch (Exception e) {
            Logger.platform.error("Migrations [%s] has failed [%s]", job.getInfo().getChangeLogPath(), job.getInfo().getName());
            Logger.platform.error(e);
            return;
        }
        Logger.platform.info("Migrations [%s] applied for [%s]", job.getInfo().getChangeLogPath(), job.getInfo().getName());
        job.getInfo().setMigrated(true);
        if (job.getCallback() != null) {
            job.getCallback().accept(job.getInfo());
        }
        counter.decrementAndGet();
    }

    @Override
    public void onError(@NonNull Throwable e) {
        // Nothing to do
        Logger.platform.error(e);
    }

    @Override
    public void onComplete() {
        // Nothing to do
    }

}
