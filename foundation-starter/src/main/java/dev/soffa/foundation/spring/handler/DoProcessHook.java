package dev.soffa.foundation.spring.handler;

import dev.soffa.foundation.context.OperationContext;
import dev.soffa.foundation.core.HookService;
import dev.soffa.foundation.core.action.ProcessHook;
import dev.soffa.foundation.core.model.ProcessHookInput;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DoProcessHook implements ProcessHook {

    private HookService hooks;
    @Override
    public Void handle(ProcessHookInput input, @NonNull OperationContext ctx) {
        hooks.process(ctx.getInternal(), input);
        return null;
    }
}
