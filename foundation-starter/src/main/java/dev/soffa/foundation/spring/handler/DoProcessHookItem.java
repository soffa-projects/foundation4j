package dev.soffa.foundation.spring.handler;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.hooks.HookService;
import dev.soffa.foundation.hooks.action.ProcessHookItem;
import dev.soffa.foundation.hooks.model.ProcessHookItemInput;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DoProcessHookItem implements ProcessHookItem {

    private HookService hooks;
    @Override
    public Void handle(ProcessHookItemInput input, @NonNull Context ctx) {
        hooks.process(ctx, input);
        return null;
    }
}
