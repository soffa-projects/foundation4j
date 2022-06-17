package dev.soffa.foundation.starter.test;

import dev.soffa.foundation.spring.config.scheduling.Scheduler;
import dev.soffa.foundation.starter.test.app.worker.SimpleJobHandler;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class SchedulingTest {

    @Autowired
    private Scheduler scheduler;

    @Test
    public void testScheduler() {
        assertNotNull(scheduler);
        Awaitility.await().atMost(12, TimeUnit.SECONDS).until(() -> SimpleJobHandler.getCount() > 0);
    }

}
