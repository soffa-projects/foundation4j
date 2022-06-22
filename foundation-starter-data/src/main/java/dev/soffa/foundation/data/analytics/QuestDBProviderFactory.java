package dev.soffa.foundation.data.analytics;

import dev.soffa.foundation.timeseries.TimeSeriesProvider;

public final class QuestDBProviderFactory {

    private QuestDBProviderFactory() {
    }

    public static TimeSeriesProvider create(String url) {
        if (url.startsWith("pg://") || url.startsWith("postgres://")) {
            return new QuestDBPgProvider(url);
        } else {
            return new QuestDBTcpProvider(url);
        }
    }
}
