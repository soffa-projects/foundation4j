package dev.soffa.foundation.data;

public interface EntityLifecycle {


    default void onInsert() {
        // Default implementation
    }

    default void onUpdate() {
        // Default implementation
    }

    default void onSave() {
        // Default implementation
    }
}
