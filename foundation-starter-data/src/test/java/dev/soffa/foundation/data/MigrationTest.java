package dev.soffa.foundation.data;

import dev.soffa.foundation.data.common.ExtDataSource;
import dev.soffa.foundation.data.migrations.Migrator;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MigrationTest {

    public static final String APP_NAME = "test-app";

    @Test
    public void testMigrationTemplate() {
        Migrator migrator = new Migrator();
        List<ExtDataSource> dataSources = new ArrayList<>();
        String changeLogPath = "classpath:db/changelog/application.xml";

        dataSources.add(ExtDataSource.create(APP_NAME, "t1", "h2://mem/t1"));
        dataSources.add(ExtDataSource.create(APP_NAME, "t2", "h2://mem/t2"));
        dataSources.add(ExtDataSource.create(APP_NAME, "t3_1", "h2://mem/t3?schema=t3_1"));
        dataSources.add(ExtDataSource.create(APP_NAME, "t3_2", "h2://mem/t3?schema=t3_2"));
        dataSources.add(ExtDataSource.create(APP_NAME, "t3_3", "h2://mem/t3?schema=t3_3"));

        dataSources.parallelStream().forEach(el -> {
            el.setChangeLogPath(changeLogPath);
            migrator.submit(el);
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> {
            System.out.println("counter = " + migrator.getCounter());
            return migrator.isEmpty();
        });

        for (ExtDataSource dataSource : dataSources) {
            assertTrue(dataSource.isMigrated());
        }

    }
}
