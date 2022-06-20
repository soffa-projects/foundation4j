package dev.soffa.foundation.data.app.action;

import dev.soffa.foundation.context.Context;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class JobAction1Handler implements JobAction1 {

    public static final AtomicBoolean FLAG = new AtomicBoolean(false);

    @Override
    public Void handle(String input, @NonNull Context ctx) {FLAG.set(true);
        return null;
    }
}
