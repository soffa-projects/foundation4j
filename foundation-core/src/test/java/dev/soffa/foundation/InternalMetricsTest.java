package dev.soffa.foundation;

import dev.soffa.foundation.metrics.InternalMetrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InternalMetricsTest {

    @Test
    public void testCounter() {
        assertEquals(0L, InternalMetrics.getCounter("foo"));
        assertEquals(1L, InternalMetrics.increment("foo"));
    }

    @Test
    public void testCounterWithPrefix() {
        assertEquals(0L, InternalMetrics.getCounter("foo", "bar"));
        assertEquals(1L, InternalMetrics.increment("foo", "bar"));
        assertEquals(2L, InternalMetrics.increment("foo", "bar"));
    }
}
