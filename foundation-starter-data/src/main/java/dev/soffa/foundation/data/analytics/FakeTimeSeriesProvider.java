package dev.soffa.foundation.data.analytics;

import dev.soffa.foundation.timeseries.DataPoint;
import dev.soffa.foundation.timeseries.TimeSeriesProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class FakeTimeSeriesProvider implements TimeSeriesProvider {

    static class LocalWriter implements Writer {

        @Override
        public void write(@NonNull List<DataPoint> points) {
            // Nothing to do here
        }

        @Override
        public void close() {
            // Nothing to do here
        }
    }

    @Override
    public Writer getWriter(String buket) {
        return new LocalWriter();
    }


}
