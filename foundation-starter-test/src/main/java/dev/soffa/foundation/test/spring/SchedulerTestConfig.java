
package dev.soffa.foundation.test.spring;

import dev.soffa.foundation.scheduling.Scheduler;
import dev.soffa.foundation.test.scheduling.DefaultTestScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

@Configuration
@ConditionalOnProperty(value = "app.test.scheduler", havingValue = "mocked")
public class SchedulerTestConfig {

    @Bean
    @Primary
    public Scheduler createDefaultTestScheduler(ApplicationContext context) {
        return new DefaultTestScheduler(context);
    }

}
