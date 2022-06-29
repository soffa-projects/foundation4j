package dev.soffa.foundation.spring.handler;

import dev.soffa.foundation.context.OperationContext;
import dev.soffa.foundation.core.HookService;
import dev.soffa.foundation.core.action.ProcessHookItem;
import dev.soffa.foundation.core.model.ProcessHookItemInput;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DoProcessHookItem implements ProcessHookItem {

    private HookService hooks;
    @Override
    public Void handle(ProcessHookItemInput input, @NonNull OperationContext ctx) {
        hooks.process(ctx.getInternal(), input);
        return null;
    }
}
