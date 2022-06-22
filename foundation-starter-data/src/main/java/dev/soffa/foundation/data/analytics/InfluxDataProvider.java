package dev.soffa.foundation.data.analytics;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import dev.soffa.foundation.commons.DateUtil;
import dev.soffa.foundation.commons.UrlInfo;
import dev.soffa.foundation.timeseries.DataPoint;
import dev.soffa.foundation.timeseries.TimeSeriesProvider;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.PreDestroy;
import java.util.List;

public class InfluxDataProvider implements TimeSeriesProvider {
    private final InfluxDBClient client;
    private final WriteApiBlocking writeApi;
    private final String org;

    public static InfluxDataProvider create(@NonNull String url) {
        UrlInfo info = UrlInfo.parse(url);
        return new InfluxDataProvider(info.getUrl(), info.getUsername(), info.getPassword());
    }

    public InfluxDataProvider(@NonNull String url, @NonNull String org, @NonNull String token) {
        client = InfluxDBClientFactory.create(url, token.toCharArray(), org);
        client.enableGzip();
        writeApi = client.getWriteApiBlocking();
        this.org = org;
    }

    @AllArgsConstructor
    static class LocalWriter implements Writer {

        private final WriteApiBlocking writeApi;
        private final String org;

        private final String bucket;

        @Override
        public void write(@NonNull List<DataPoint> points) {
            for (DataPoint point : points) {
                Point p = Point.measurement(point.getMetric())
                    .addFields(point.getFields())
                    .addTags(point.getTags())
                    .time(DateUtil.nano(point.getTime()), WritePrecision.NS);
                writeApi.writePoint(bucket, this.org, p);
            }
        }

        @Override
        public void close() {
            // nothing to do, writer is a singleont
        }
    }


    @PreDestroy
    @Override
    public void close() {
        client.close();
    }

    @Override
    public Writer getWriter(String bucket) {
        return new LocalWriter(writeApi, this.org, bucket);
    }
}
