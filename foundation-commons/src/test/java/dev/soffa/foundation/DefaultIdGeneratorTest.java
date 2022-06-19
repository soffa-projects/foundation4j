package dev.soffa.foundation;

import dev.soffa.foundation.commons.DefaultIdGenerator;
import dev.soffa.foundation.error.TechnicalException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultIdGeneratorTest {

    public static final String CH = "ch";
    public static final String LIVE = "live";
    public static final String LIVE_ = "_" + LIVE;
    public static final String CH_ = CH + "_";
    public static final String CH_LIVE = "ch_live_";

    @SneakyThrows
    @Test
    public void testIdCollisions() {
        final int count = 10_000;
        int threads = 100;
        final Map<String, Boolean> consumed = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threads * count);
        for (int t = 0; t < threads; t++) {
            executor.execute(() -> {
                for (int c = 0; c < count; c++) {
                    String value = DefaultIdGenerator.uuidSnakeCase();
                    if (consumed.containsKey(value)) {
                        throw new TechnicalException("IdGenerator.uuid() returned a duplicate value");
                    }
                    consumed.put(value, true);
                    latch.countDown();
                }
            });
        }
        assertTrue(latch.await(20, TimeUnit.SECONDS));
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testIdGenerator() {
        assertTrue(DefaultIdGenerator.secret(CH).startsWith(CH_));
        assertTrue(DefaultIdGenerator.secret(CH_).startsWith(CH_));

        assertTrue(DefaultIdGenerator.secret(CH, LIVE).startsWith(CH_LIVE));
        assertTrue(DefaultIdGenerator.secret(CH, LIVE_).startsWith(CH_LIVE));
        assertTrue(DefaultIdGenerator.secret(CH_, LIVE_).startsWith(CH_LIVE));
        assertTrue(DefaultIdGenerator.secret(CH_, LIVE).startsWith(CH_LIVE));
    }

}
