package dev.soffa.foundation.data.spring.scheduling;

import dev.soffa.foundation.scheduling.ServiceWorker;
import lombok.Value;

@Value
class RecurringJob {

    String cronId;
    String cron;
    ServiceWorker worker;

}
