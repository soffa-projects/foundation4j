package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.scheduling.OperationScheduler;
import dev.soffa.foundation.spring.service.SimpleExecutorOperationScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {

    @Bean
    @ConditionalOnMissingBean(OperationScheduler.class)
    public OperationScheduler createDefaultScheduler(ApplicationContext context) {
        return new SimpleExecutorOperationScheduler(context);
    }
}
