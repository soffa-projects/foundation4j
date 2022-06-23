package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.activity.ActivityService;
import dev.soffa.foundation.spring.service.DefaultActivityService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivitiesBeanConfig {

    @Bean
    @ConditionalOnMissingBean(ActivityService.class)
    public ActivityService createDefaultActivityService() {
        return new DefaultActivityService();
    }
}
