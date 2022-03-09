package dev.soffa.foundation.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthStatusTest {

    @Test
    public void testHealthStatus() {
        assertEquals(3, HealthStatus.values().length);
        assertEquals("UP", HealthStatus.UP.name());
        assertEquals("DOWN", HealthStatus.DOWN.name());
        assertEquals("UNKNOWN", HealthStatus.UNKNOWN.name());
    }
}
