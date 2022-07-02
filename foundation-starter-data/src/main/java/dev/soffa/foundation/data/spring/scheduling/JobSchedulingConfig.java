package dev.soffa.foundation.data.spring.scheduling;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.data.DB;
import lombok.SneakyThrows;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.jobs.filters.RetryFilter;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
public class JobSchedulingConfig {

    private static final Logger LOG = Logger.get(JobSchedulingConfig.class);
    @SneakyThrows
    @Bean
    @SuppressWarnings("PMD.CloseResource")
    public JobScheduler createJobScheduling(ApplicationContext context,
                                            DB db,
                                            @Value("${app.scheduler.dashbort.port:${SCHEDULER_DASHBOARD_PORT:}}") String dashboardPort,
                                            @Autowired(required = false) DataSource dataSource) {

        Preconditions.checkNotNull(db.getDefaultDataSource(), "No default datasource found");
        StorageProvider storage;
        if (dataSource != null) {
            storage = SqlStorageProviderFactory.using(db.getDefaultDataSource());
        } else {
            storage = new InMemoryStorageProvider();
        }
        JobRunrConfiguration config = JobRunr.configure()
            .useStorageProvider(storage)
            .withJobFilter(new RetryFilter(20, 3))
            .useJobActivator(context::getBean)
            .useBackgroundJobServer(3);

        if (TextUtil.isEmpty(dashboardPort)) {
            LOG.info("Scheduler dashboard is disabled (because port is empty)");
        }else {
            config.useDashboard(Integer.parseInt(dashboardPort));
            LOG.info("Scheduler dashboard will be available on port %s", dashboardPort);
        }

        return config.initialize().getJobScheduler();
    }
}
