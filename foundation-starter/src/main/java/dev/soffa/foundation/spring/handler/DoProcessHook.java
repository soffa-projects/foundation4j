package dev.soffa.foundation.spring.handler;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.hooks.HookService;
import dev.soffa.foundation.hooks.action.ProcessHook;
import dev.soffa.foundation.hooks.model.ProcessHookInput;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DoProcessHook implements ProcessHook {

    private HookService hooks;
    @Override
    public Void handle(ProcessHookInput input, @NonNull Context ctx) {
        hooks.process(ctx, input);
        return null;
    }
}
