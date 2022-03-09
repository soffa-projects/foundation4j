package dev.soffa.foundation.models;

import org.checkerframework.checker.nullness.qual.NonNull;

public class ScopedSideEffects {

    private final SideEffects parent;
    private final String kind;
    private final String subject;

    ScopedSideEffects(SideEffects parent, String kind, String subject) {
        this.parent = parent;
        this.kind = kind;
        this.subject = subject;
    }

    public void add(@NonNull String... events) {
        for (String event : events) {
            parent.getEffects().add(new SideEffect(kind, subject, event));
        }
    }
}
