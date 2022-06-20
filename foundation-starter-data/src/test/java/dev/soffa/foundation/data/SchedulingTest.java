package dev.soffa.foundation.data;

import dev.soffa.foundation.data.app.action.JobAction1;
import dev.soffa.foundation.data.app.action.JobAction1Handler;
import dev.soffa.foundation.data.app.worker.SimpleJobHandler;
import dev.soffa.foundation.scheduling.Scheduler;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class SchedulingTest {

    @Inject
    private Scheduler scheduler;

    @Test
    public void testScheduler() {
        assertNotNull(scheduler);
        Awaitility.await().atMost(12, TimeUnit.SECONDS).until(() -> SimpleJobHandler.getCount() > 0);
    }
   @Test
    public void testEnqueue() {
       scheduler.enqueue(JobAction1.class, "foo");
       Awaitility.await().atMost(15, TimeUnit.SECONDS).until(JobAction1Handler.FLAG::get);
    }

}
