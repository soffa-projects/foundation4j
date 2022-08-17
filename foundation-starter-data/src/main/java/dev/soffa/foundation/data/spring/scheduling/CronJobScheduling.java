package dev.soffa.foundation.data.spring.scheduling;

import dev.soffa.foundation.annotation.Cron;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.config.OperationsMapping;
import dev.soffa.foundation.context.ApplicationLifecycle;
import dev.soffa.foundation.scheduling.OperationScheduler;
import dev.soffa.foundation.scheduling.ServiceWorker;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
class CronJobScheduling implements ApplicationLifecycle {

    private final OperationScheduler scheduler;
    private final ApplicationContext context;

    @Override
    public void onApplicationReady() {
        Map<String, ServiceWorker> workers = context.getBeansOfType(ServiceWorker.class);

        if (workers.isEmpty()) {
            Logger.platform.info("No ServiceWorker found in current context");
            return;
        }

        Logger.platform.info("%s ServiceWorker(s) found in current context", workers.size());

        int count = 0;
        for (ServiceWorker worker : workers.values()) {
            String cron = Cron.EVERY_30_SECONDS;
            if (worker.getClass().isAnnotationPresent(Cron.class)) {
                Cron annotation = worker.getClass().getAnnotation(Cron.class);
                cron = annotation.value();
            }
            String cronId = OperationsMapping.resolveClass(worker.getClass()).getSimpleName().split("\\$")[0];
            Logger.platform.info("Scheduling worker %s[%s]", cronId, worker.getClass());
            count++;
            scheduler.scheduleRecurrently(cronId, cron, worker);
        }
        if (count == 0) {
            Logger.platform.info("No @Cron annotated method found in service workers");
        }
    }


}
