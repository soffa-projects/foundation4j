package dev.soffa.foundation.data.spring;

import dev.soffa.foundation.data.analytics.FakeTimeSeriesProvider;
import dev.soffa.foundation.data.analytics.InfluxDataProvider;
import dev.soffa.foundation.data.analytics.QuestDBProviderFactory;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.timeseries.TimeSeriesProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "app.data.timeseries.enabled", havingValue = "true")
public class TimeSeriesProviderConfig {


    @Bean
    public TimeSeriesProvider createTimeSeriesProvider(@Value("${app.data.timeseries.provider}") String provider) {
        if ("none".equalsIgnoreCase(provider)) {
            return new FakeTimeSeriesProvider();
        }
        if (provider.startsWith("influxdb:")) {
            return InfluxDataProvider.create(provider.replace("influxdb:", ""));
        }
        if (provider.startsWith("questdb:")) {
            return QuestDBProviderFactory.create(provider.replace("questdb:", ""));
        }
        throw new TechnicalException("Unsupported time series provider: " + provider);
    }

}
