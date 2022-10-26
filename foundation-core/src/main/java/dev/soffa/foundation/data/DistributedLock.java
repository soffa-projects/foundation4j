package dev.soffa.foundation.data;

import java.time.Duration;

public interface DistributedLock {

    /**
     * Lock with detaul values (at least 1 minute, at most 5 minutes)
     * @param name Name of the lock
     * @param runnable Runnable to execute
     */
    default void withLock(String name, Runnable runnable) {
        withLock(name, Duration.ofMinutes(10), Duration.ofMinutes(5), runnable);
    }

    default void withLock(String name, int atMostSeconds, int atLeastSeconds, Runnable runnable) {
        withLock(name, Duration.ofSeconds(atMostSeconds), Duration.ofSeconds(atLeastSeconds), runnable);
    }

    void withLock(String name, Duration atMost, Duration atLeast, Runnable runnable);

}
