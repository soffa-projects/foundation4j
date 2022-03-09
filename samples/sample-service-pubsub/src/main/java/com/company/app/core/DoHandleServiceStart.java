package com.company.app.core;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.events.OnServiceStarted;
import dev.soffa.foundation.events.ServiceInfo;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Named;
import java.util.concurrent.atomic.AtomicBoolean;

@Named
public class DoHandleServiceStart implements OnServiceStarted {

    public static final AtomicBoolean RECEIVED = new AtomicBoolean(false);

    @Override
    public Void handle(ServiceInfo input, @NonNull Context ctx) {
        RECEIVED.set(true);
        return null;
    }

}
