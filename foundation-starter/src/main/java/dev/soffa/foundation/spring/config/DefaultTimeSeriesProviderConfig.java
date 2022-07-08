package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.timeseries.NoopTimeSeriesProvider;
import dev.soffa.foundation.timeseries.TimeSeriesProvider;

// @Configuration
// @ConditionalOnMissingBean(TimeSeriesProvider.class)
public class DefaultTimeSeriesProviderConfig {

    // @Bean
    // @ConditionalOnMissingBean(TimeSeriesProvider.class)
    public TimeSeriesProvider createDefaultTimeSeriesProvider() {
        return new NoopTimeSeriesProvider();
    }

}
