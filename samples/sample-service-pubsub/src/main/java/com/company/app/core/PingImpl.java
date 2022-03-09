package com.company.app.core;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.errors.FakeException;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Named;


@Named
public class PingImpl implements Ping {

    public static final String T2 = "T2";

    @Override
    public PingResponse handle(Void arg, @NonNull Context ctx) {
        if (T2.equals(ctx.getTenantId())) {
            throw new FakeException("Controlled error triggered (%s)", ctx.getTenantId());
        } else {
            return new PingResponse("PONG");
        }
    }

}
