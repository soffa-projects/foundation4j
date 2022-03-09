package dev.soffa.foundation.spring.config.jobs;

/*
import dev.soffa.foundation.messages.MessageHandler;
import dev.soffa.foundation.platform.spring.data.DBImpl;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.jobs.filters.RetryFilter;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(value = "app.jobs.enabled", havingValue = "true")
public class JobsRunrConfig {

    @Bean
    @Primary
    public JobManager createJobManager(DataSource ds, MessageHandler handler,
                                       ApplicationContext applicationContext,
                                       @Value("${app.jobs.retries:10}") int retries) {
        DataSource target = ds;
        if (ds instanceof DBImpl) {
            target = ((DBImpl) ds).getDefault();
        }
        JobRunrConfiguration.JobRunrConfigurationResult config = JobRunr
            .configure()
            .withJobFilter(new RetryFilter(retries))
            .useStorageProvider(SqlStorageProviderFactory.using(target))
            .useJobActivator(applicationContext::getBean)
            .useBackgroundJobServer()
            .useJmxExtensions()
            .useDashboard()
            .initialize();
        return new JobManager(handler, config);
    }
}


 */
