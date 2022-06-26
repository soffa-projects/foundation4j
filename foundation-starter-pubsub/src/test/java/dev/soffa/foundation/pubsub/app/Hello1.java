package dev.soffa.foundation.pubsub.app;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class Hello1 implements EventHandler<Void, String> {

    private static final AtomicLong COUNTER = new AtomicLong(0);

    @Override
    public String handle(Void input, @NonNull Context ctx) {
        COUNTER.incrementAndGet();
        return "Hello";
    }

    public static long count() {
        return COUNTER.get();
    }
}
