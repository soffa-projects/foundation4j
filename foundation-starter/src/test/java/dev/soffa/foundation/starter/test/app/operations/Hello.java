package dev.soffa.foundation.starter.test.app.operations;

import dev.soffa.foundation.annotations.Handle;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.models.Ack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Handle("HELLO")
public class Hello implements Operation<Void, Ack> {

    public static final AtomicInteger TICK = new AtomicInteger(0);

    @Override
    public Ack handle(Void input, @NonNull Context ctx) {
        TICK.incrementAndGet();
        return Ack.OK;
    }
}
