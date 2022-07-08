package dev.soffa.foundation.data.spring;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.data.analytics.InfluxDataProvider;
import dev.soffa.foundation.data.analytics.QuestDBProviderFactory;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.timeseries.NoopTimeSeriesProvider;
import dev.soffa.foundation.timeseries.TimeSeriesProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(value = "app.data.timeseries.enabled", havingValue = "true")
public class TimeSeriesProviderConfig {


    @Bean
    @Primary
    public TimeSeriesProvider createTimeSeriesProvider(@Value("${app.data.timeseries.provider}") String provider) {
        if ("none".equalsIgnoreCase(provider)) {
            return new NoopTimeSeriesProvider();
        }
        if (provider.startsWith("influxdb:")) {
            Logger.platform.info("Using InfluxDB as time series provider");
            return InfluxDataProvider.create(provider.replace("influxdb:", ""));
        }
        if (provider.startsWith("questdb:")) {
            Logger.platform.info("Using QuestDB as time series provider");
            return QuestDBProviderFactory.create(provider.replace("questdb:", ""));
        }
        throw new TechnicalException("Unsupported time series provider: " + provider);
    }

}
