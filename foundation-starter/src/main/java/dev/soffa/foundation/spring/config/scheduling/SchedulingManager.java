package dev.soffa.foundation.spring.config.scheduling;

import dev.soffa.foundation.annotation.Cron;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.context.ApplicationLifecycle;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.scheduling.ServiceWorker;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.jobs.filters.RetryFilter;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnBean(ServiceWorker.class)
class SchedulingManager implements ApplicationLifecycle, Scheduler {

    private static final Logger LOG = Logger.get(SchedulingManager.class);
    public static final int RETRIES = 3;

    private final Map<String, ServiceWorker> workers = new HashMap<>();
    private final JobScheduler jobScheduler;

    @SuppressWarnings("PMD.CloseResource")
    public SchedulingManager(ApplicationContext context,
                             List<ServiceWorker> workers,
                             @Autowired(required = false) DataSource dataSource) {
        StorageProvider storage;
        if (dataSource != null) {
            storage = SqlStorageProviderFactory.using(dataSource);
        } else {
            storage = new InMemoryStorageProvider();
        }
        jobScheduler = JobRunr.configure()
            .useStorageProvider(storage)
            .withJobFilter(new RetryFilter(RETRIES))
            .useJobActivator(context::getBean)
            .useBackgroundJobServer()
            //.useJmxExtensions()
            //.useDashboard()
            .initialize()
            .getJobScheduler();

        for (ServiceWorker worker : workers) {
            this.workers.put(worker.getClass().getName(), worker);
        }
    }

    @Override
    public void onApplicationReady() {
        LOG.info("Looking for @Cron annotated method in service workers");
        int count = 0;
        for (final ServiceWorker worker : workers.values()) {
            String cron = Cron.EVERY_30_SECONDS;
            if (worker.getClass().isAnnotationPresent(Cron.class)) {
                Cron annotation = worker.getClass().getAnnotation(Cron.class);
                cron = annotation.value();
            }
            String cronId = worker.getClass().getSimpleName();
            LOG.info("[Scheduling] Worker registered: %s", worker.getClass());
            count++;
            jobScheduler.scheduleRecurrently(cronId, cron, worker::tick);
        }
        if (count == 0) {
            LOG.warn("No @Cron annotated method found in service workers");
        }
    }

    public void enqueue(Runnable runnable) {
        TenantHolder.useDefault(() -> {
            jobScheduler.enqueue(runnable::run);
            return Optional.empty();
        });
    }
}
