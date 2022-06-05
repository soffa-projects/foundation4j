package dev.soffa.foundation.spring.config.scheduling;

import dev.soffa.foundation.annotation.Cron;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.context.ApplicationLifecycle;
import dev.soffa.foundation.scheduling.ServiceWorker;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.MethodUtils;
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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnBean(ServiceWorker.class)
public class Scheduler implements ApplicationLifecycle {

    private static final Logger LOG = Logger.get(Scheduler.class);
    public static final int RETRIES = 3;

    private final Map<String, ServiceWorker> workers = new HashMap<>();
    private final JobScheduler jobScheduler;

    @SuppressWarnings("PMD.CloseResource")
    public Scheduler(ApplicationContext context,
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
            for (final Method method : worker.getClass().getMethods()) {
                if (method.isAnnotationPresent(Cron.class)) {
                    String cronId = TextUtil.format("%s__%s", worker.getClass().getSimpleName(), method.getName());
                    LOG.info("[Scheduling] Method %s.%s registered", worker.getClass().getSimpleName(), method.getName());
                    count++;
                    final String methodName = method.getName();
                    Cron annotation = method.getAnnotation(Cron.class);
                    String workerName = worker.getClass().getName();
                    jobScheduler.scheduleRecurrently(cronId, annotation.value(), () -> this.triggerJob(workerName, methodName));

                }
            }
        }
        if (count == 0) {
            LOG.warn("No @Cron annotated method found in service workers");
        }
    }

    @SneakyThrows
    public void triggerJob(String className, String methodName) {
        MethodUtils.invokeMethod(workers.get(className), methodName, new Object[]{});
    }
}
