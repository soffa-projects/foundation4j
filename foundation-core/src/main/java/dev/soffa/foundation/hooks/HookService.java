package dev.soffa.foundation.hooks;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.hooks.model.Hook;
import dev.soffa.foundation.hooks.model.ProcessHookInput;
import dev.soffa.foundation.hooks.model.ProcessHookItemInput;

public interface HookService extends Hooks{


    Hook getHook(String operationId);
    int process(Context context, ProcessHookInput input);
    void process(Context context, ProcessHookItemInput input);


}
