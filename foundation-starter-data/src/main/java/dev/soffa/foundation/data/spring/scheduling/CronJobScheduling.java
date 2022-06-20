package dev.soffa.foundation.data.spring.scheduling;

import dev.soffa.foundation.annotation.Cron;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.context.ApplicationLifecycle;
import dev.soffa.foundation.scheduling.Scheduler;
import dev.soffa.foundation.scheduling.ServiceWorker;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
class CronJobScheduling implements ApplicationLifecycle {

    private static final Logger LOG = Logger.get(CronJobScheduling.class);
    private final Scheduler scheduler;
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
            scheduler.scheduleRecurrently(cronId, cron, worker);
        }
        if (count == 0) {
            LOG.warn("No @Cron annotated method found in service workers");
        }
    }



}
