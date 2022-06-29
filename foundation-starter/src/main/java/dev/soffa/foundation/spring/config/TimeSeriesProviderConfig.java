package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.timeseries.NoopTimeSeriesProvider;
import dev.soffa.foundation.timeseries.TimeSeriesProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeSeriesProviderConfig {

    @Bean
    @ConditionalOnMissingBean(TimeSeriesProvider.class)
    public TimeSeriesProvider createDefaultTimeSeriesProvider() {
        return new NoopTimeSeriesProvider();
    }

}
