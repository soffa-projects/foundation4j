package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.core.Dispatcher;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@AllArgsConstructor
public class ModelAttributeAdvices {

    private final Dispatcher dispatcher;

    @ModelAttribute
    public Context createRequestContextAttribute() {
        return ContextHolder.inheritOrCreate();
    }

    @ModelAttribute
    public Dispatcher createOperationDispatcherAttribute() {
        return dispatcher;
    }

}
