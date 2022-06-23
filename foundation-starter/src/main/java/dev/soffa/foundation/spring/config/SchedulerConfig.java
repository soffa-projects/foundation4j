package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.scheduling.Scheduler;
import dev.soffa.foundation.spring.service.SimpleExecutorScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {

    @Bean
    @ConditionalOnMissingBean(Scheduler.class)
    public Scheduler createDefaultScheduler(ApplicationContext context) {
        return new SimpleExecutorScheduler(context);
    }
}
