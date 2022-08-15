package dev.soffa.foundation.pubsub.app;

import dev.soffa.foundation.context.OperationContext;
import dev.soffa.foundation.core.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class Broadcast1 implements EventHandler<Void, String> {

    private static final AtomicLong COUNTER = new AtomicLong(0);

    public static long count() {
        return COUNTER.get();
    }

    @Override
    public String handle(Void input, @NonNull OperationContext ctx) {
        COUNTER.incrementAndGet();
        return "Broadcaster";
    }
}
