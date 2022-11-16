package dev.soffa.foundation.data;

import dev.soffa.foundation.commons.DateUtil;
import dev.soffa.foundation.commons.RandomUtil;
import dev.soffa.foundation.data.analytics.InfluxDataProvider;
import dev.soffa.foundation.data.app.model.PaymentMetric;
import dev.soffa.foundation.helper.ID;
import dev.soffa.foundation.model.DataPoint;
import dev.soffa.foundation.timeseries.TSFieldType;
import dev.soffa.foundation.timeseries.TSTable;
import dev.soffa.foundation.timeseries.TimeSeriesProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class TimeSeriesProviderTest {

    public static final int RECORDS = 100;
    public static final String PAYMENTS_METRIC = "payments";

    @Test
    @Disabled
    public void testInfluxDataProvider() throws IOException {

        TimeSeriesProvider client = InfluxDataProvider.create("http://foo:foo@localhost:8086");
        try (TimeSeriesProvider.Writer writer = client.getWriter("vitepay")) {
            for (int i = 0; i < RECORDS; i++) {
                DataPoint point = DataPoint.metric(PAYMENTS_METRIC)
                    .addField("id", i)
                    .addTag("account", "acc_001")
                    .addTag("tenant", "apt_001")
                    .addTag("application", "app_01")
                    .addField("amount", RandomUtil.nextInt(1000, 10_000))
                    .addTag("status", Math.random() > 0.8 ? "success" : "failed")
                    .addTag("network", Math.random() > 0.7 ? "orange_money" : "sama_money");
                writer.write(point);
            }
        }
    }

    @Test
    @Disabled
    public void testQuestDBJdbcProvider() throws IOException {

        TimeSeriesProvider client = QuestDBProviderFactory.create("pg://admin:quest@localhost:8812/qdb");

        TSTable table = new TSTable(PAYMENTS_METRIC)
            .field("id")
            .field("tenant", TSFieldType.SYMBOL)
            .field("application", TSFieldType.SYMBOL)
            .field("account", TSFieldType.SYMBOL)
            .field("amount", TSFieldType.DOUBLE)
            .field("status", TSFieldType.SYMBOL)
            .field("payment_method", TSFieldType.SYMBOL)
            .timestamp("time");

        client.createTable(table);

        String[] merchants = {"merchant1", "merchant2", "merchant3", "merchant4", "merchant5"};

        try (TimeSeriesProvider.Writer writer = client.getWriter(PAYMENTS_METRIC)) {

            Instant startDate = Instant.now().minus(Duration.ofDays(30 * 6));
            Instant endDate = Instant.now();

            for (int i = 0; i < RECORDS; i++) {
                Date time = RandomUtil.nextDate(
                    startDate, // 6 month ago
                    endDate
                );
                PaymentMetric point = new PaymentMetric(
                    DateUtil.nano(time),
                    ID.generate(),
                    RandomUtil.nextString(merchants),
                    "app",
                    "bantu",
                    RandomUtil.nextInt(100, 10_000),
                    Math.random() < 0.25 ? "failed" : "paid",
                    Math.random() < 0.25 ? "sama_money" : "orange_money"
                );
                writer.writeRecords(point);
            }
        }
    }

    @Test
    @Disabled
    public void testQuestDBTcpProvider() throws IOException {

        TimeSeriesProvider tpcClient = QuestDBProviderFactory.create("127.0.0.1:9009");
        try (TimeSeriesProvider.Writer writer = tpcClient.getWriter()) {
            for (int i = 0; i < RECORDS; i++) {
                DataPoint point = DataPoint.metric(PAYMENTS_METRIC)
                    .addField("id", ID.generate())
                    .addTag("account", "acc_001")
                    .addTag("tenant", "apt_001")
                    .addTag("application", "app_01")
                    .addField("amount", RandomUtil.nextInt(1000, 10_000))
                    .addTag("status", Math.random() > 0.8 ? "success" : "failed")
                    .addTag("network", Math.random() > 0.7 ? "net_1" : "net_2");
                writer.write(point);
            }
        }


    }
}
