package dev.soffa.foundation.spring.config.scheduling;

import dev.soffa.foundation.annotation.Cron;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.context.ApplicationLifecycle;
import dev.soffa.foundation.scheduling.ServiceWorker;
import lombok.AllArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
class CronJobScheduling implements ApplicationLifecycle {

    private static final Logger LOG = Logger.get(CronJobScheduling.class);
    private final JobScheduler jobScheduler;
    private final ApplicationContext context;

    @Override
    public void onApplicationReady() {
        Map<String,ServiceWorker> workers = context.getBeansOfType(ServiceWorker.class);

        if (workers.isEmpty()) {
            LOG.info("No ServiceWorker found in current context");
            return;
        }

        LOG.info("%s ServiceWorker(s) found in current context", workers.size());

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



}
