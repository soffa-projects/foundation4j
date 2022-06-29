package dev.soffa.foundation.timeseries;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.model.DataPoint;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class NoopTimeSeriesProvider implements TimeSeriesProvider {

    static class LocalWriter implements Writer {

        @Override
        public void write(@NonNull List<DataPoint> points) {
            // Nothing to do here
            Logger.platform.info("[noop-timeseries] %d data points written", points.size());
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
