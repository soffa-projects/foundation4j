package dev.soffa.foundation.data.analytics;

import dev.soffa.foundation.data.DataStore;
import dev.soffa.foundation.data.SimpleDataStore;
import dev.soffa.foundation.timeseries.TSField;
import dev.soffa.foundation.timeseries.TSTable;
import dev.soffa.foundation.timeseries.TimeSeriesProvider;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class QuestDBPgProvider implements TimeSeriesProvider {

    private final DataStore ds;

    public QuestDBPgProvider(@NonNull String url) {
        this.ds = SimpleDataStore.create(url);
    }

    @AllArgsConstructor
    static class LocalWriter implements Writer {

        private final DataStore ds;
        private final String bucket;

        @Override
        public <E> void writeRecords(@NonNull List<E> records) {
            ds.batch(bucket, records);
        }

        @Override
        public void close() {
            // Nothing to do
        }
    }

    @Override
    public TimeSeriesProvider.Writer getWriter(String bucket) {
        return new LocalWriter(ds, bucket);
    }

    @Override
    public void createTable(TSTable table) {
        StringBuilder buffer = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(table.getName()).append('(');
        int index = 0;
        for (TSField field : table.getFields().values()) {
            StringBuilder lb = new StringBuilder();
            if (index>0) {
                lb.append(", ");
            }
            lb.append(field.getName()).append(' ').append(field.getType().name());
            if (field.isIndexed()) {
                lb.append(" index");
            }
            buffer.append(lb);
            index++;
        }
        buffer.append(") TIMESTAMP(").append(table.getTimestampField()).append(") PARTITION BY day");
        ds.execute(buffer.toString());
    }

    @Override
    public void close() {
        //Nothing to do
    }

}
