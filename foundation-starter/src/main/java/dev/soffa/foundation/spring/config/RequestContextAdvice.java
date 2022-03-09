package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class RequestContextAdvice {

    @ModelAttribute
    public Context createRequestContextAttribute() {
        return ContextHolder.inheritOrCreate();
    }

}
