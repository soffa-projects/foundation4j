package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.model.HookSpec;
import dev.soffa.foundation.core.model.ProcessHookInput;
import dev.soffa.foundation.core.model.ProcessHookItemInput;

public interface HookService extends Hooks{

    HookSpec getHook(String operationId);
    int process(Context context, ProcessHookInput input);
    void process(Context context, ProcessHookItemInput input);
    long getProcessedHooks();


}
