package dev.soffa.foundation.commons;

import dev.soffa.foundation.errors.TechnicalException;
import lombok.SneakyThrows;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public final class ExecutorHelper {

    private static final ExecutorService SC = Executors.newCachedThreadPool();

    private ExecutorHelper() {
    }

    public static Future<?> submit(final Runnable runnable) {
        return SC.submit(runnable);
    }

    public static void execute(final Runnable runnable) {
        SC.execute(runnable);
    }

    @SneakyThrows
    public static void await(final Runnable runnable) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> exception = new AtomicReference<>(null);
        SC.execute(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(5, TimeUnit.MINUTES)) {
            throw new TechnicalException("BindOperation timed out");
        }
        if (exception.get() != null) {
            throw exception.get();
        }
    }


}
