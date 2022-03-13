package dev.soffa.foundation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;


@NoArgsConstructor
public class SideEffects {

    private final List<SideEffect> effects = new ArrayList<>();

    public String getObject() {
        return "side_effects";
    }

    @JsonIgnore
    public boolean isEmpty() {
        return effects.isEmpty();
    }

    public SideEffects addAll(@NonNull SideEffects effects) {
        for (SideEffect effect : effects.getEffects()) {
            add(effect);
        }
        return this;
    }

    public void with(@NonNull String kind, @NonNull String subject, Consumer<ScopedSideEffects> consumer) {
        ScopedSideEffects scoped = new ScopedSideEffects(this, kind, subject);
        consumer.accept(scoped);
    }

    public ScopedSideEffects of(@NonNull String kind, @NonNull String subject) {
        return new ScopedSideEffects(this, kind, subject);
    }

    public SideEffects add(@NonNull SideEffect... effects) {
        this.effects.addAll(Arrays.asList(effects));
        return this;
    }

    public List<SideEffect> getEffects() {
        return effects;
    }

}
