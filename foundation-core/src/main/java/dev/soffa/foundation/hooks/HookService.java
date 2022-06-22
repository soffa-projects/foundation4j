package dev.soffa.foundation.hooks;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.hooks.action.ProcessHookInput;
import dev.soffa.foundation.hooks.model.Hook;

public interface HookService {

    Hook getHook(String operationId);

    int process(Context context, ProcessHookInput input);

}
