package dev.soffa.foundation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class SideEffect {

    private String kind;
    private String subjet;
    private String event;
    private String error;
    private Map<String, Object> metadata;

    public SideEffect(@NonNull String subjet, @NonNull String event) {
        this.subjet = subjet;
        this.event = event;
    }

    public SideEffect(@NonNull String kind, @NonNull String subjet, @NonNull String event) {
        this.kind = kind;
        this.subjet = subjet;
        this.event = event;
    }

    public SideEffect(@NonNull String event) {
        this.event = event;
    }

    public SideEffect(@NonNull String event, @NonNull Map<String, Object> metadata) {
        this.event = event;
        this.metadata = metadata;
    }
}
