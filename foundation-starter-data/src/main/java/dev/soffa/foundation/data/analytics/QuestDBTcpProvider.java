package dev.soffa.foundation.data.analytics;

import dev.soffa.foundation.commons.DateUtil;
import dev.soffa.foundation.commons.UrlInfo;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.timeseries.DataPoint;
import dev.soffa.foundation.timeseries.TimeSeriesProvider;
import io.questdb.cutlass.line.AbstractLineSender;
import io.questdb.cutlass.line.LineTcpSender;
import io.questdb.network.Net;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;

public class QuestDBTcpProvider implements TimeSeriesProvider {

    public static final int BUFFER_CAPACITY = 1000;
    private final int host;
    private final int port;

    public QuestDBTcpProvider(@NonNull String hostname) {
        UrlInfo info = UrlInfo.parse(hostname);
        this.host = Net.parseIPv4(info.getHostname());
        this.port = info.getPort();
    }

    @AllArgsConstructor
    static class LocalWriter implements Writer {
        private LineTcpSender sender;

        @Override
        @SuppressWarnings("PMD.CloseResource")
        public void write(@NonNull List<DataPoint> points) {
            for (DataPoint point : points) {
                AbstractLineSender ls = sender.metric(point.getMetric());
                for (Map.Entry<String, String> e : point.getTags().entrySet()) {
                    ls.tag(e.getKey(), e.getValue());
                }
                for (Map.Entry<String, Object> e : point.getFields().entrySet()) {
                    Object value = e.getValue();
                    if (value instanceof Boolean) {
                        ls.field(e.getKey(), (boolean) value);
                    } else if (value instanceof Double) {
                        ls.field(e.getKey(), (double) value);
                    } else if (value instanceof Long) {
                        ls.field(e.getKey(), (long) value);
                    } else if (value instanceof CharSequence) {
                        ls.field(e.getKey(), (CharSequence) value);
                    } else {
                        throw new TechnicalException("Invalid field type: %s", value.getClass().getSimpleName());
                    }
                }
                ls.$(DateUtil.nano(point.getTime()));
            }
        }

        @Override
        public void close() {
            sender.close();
        }
    }

    @Override
    public Writer getWriter(String ignore) {
        return new LocalWriter(new LineTcpSender(host, port, BUFFER_CAPACITY));
    }

    @Override
    public void close() {
        //Nothing to do
    }

}
