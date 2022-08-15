package dev.soffa.foundation.timeseries;

import dev.soffa.foundation.error.TodoException;
import dev.soffa.foundation.model.DataPoint;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;

public interface TimeSeriesProvider {

    default void createTable(TSTable table) {
        throw new TodoException();
    }

    Writer getWriter(String buket);

    default Writer getWriter() {
        return getWriter(null);
    }

    default void close() {
        // Nothing to do
    }

    interface Writer extends Closeable {
        default void write(@NonNull DataPoint point) {
            write(Collections.singletonList(point));
        }

        default void writeRecords(@NonNull Object model) {
            writeRecords(Collections.singletonList(model));
        }

        default void write(@NonNull List<DataPoint> points) {
            throw new TodoException("Feature not implemented");
        }

        default <E> void writeRecords(@NonNull List<E> model) {
            throw new TodoException("Feature not implemented");
        }


    }
}
