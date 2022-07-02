package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.model.HookEntry;

import java.util.Map;

public interface Hooks {

    void enqueue(String hook, String subjectId, Map<String, Object> data, Context context);

    default void enqueue(HookEntry hook, Context context) {
        enqueue(hook.getId(), hook.getSubject(), hook.getData(), context);
    }



}
