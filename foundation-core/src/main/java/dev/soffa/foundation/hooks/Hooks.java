package dev.soffa.foundation.hooks;

import dev.soffa.foundation.context.Context;

import java.util.Map;

public interface Hooks {

    void enqueue(String hook, String subjectId, Map<String, Object> data, Context context);


}
