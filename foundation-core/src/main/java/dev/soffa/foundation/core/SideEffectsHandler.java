package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.OperationSideEffects;

public interface SideEffectsHandler {


    void enqueue(String operationName, String uuid, OperationSideEffects sideEffects, Context context);


}
