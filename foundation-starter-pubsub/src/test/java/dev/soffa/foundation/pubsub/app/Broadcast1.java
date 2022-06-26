package dev.soffa.foundation.pubsub.app;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class Broadcast1 implements EventHandler<Void, String> {

    private static final AtomicLong counter = new AtomicLong(0);

    @Override
    public String handle(Void input, @NonNull Context ctx) {
        counter.incrementAndGet();
        return "Broadcaster";
    }

    public static long count() {
        return counter.get();
    }
}
