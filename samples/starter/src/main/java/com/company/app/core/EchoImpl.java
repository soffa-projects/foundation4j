package com.company.app.core;

import dev.soffa.foundation.context.Context;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Named;


@Named
public class EchoImpl implements Echo {

    @Override
    public String handle(@NonNull String input, @NonNull Context ctx) {
        return input;
    }

}
