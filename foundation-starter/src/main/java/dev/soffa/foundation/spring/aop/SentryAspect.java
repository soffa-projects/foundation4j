package dev.soffa.foundation.spring.aop;

import dev.soffa.foundation.annotation.Sentry;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Aspect
@Component
public class SentryAspect {

    @SuppressWarnings("Convert2Lambda")
    @SneakyThrows
    @Around("@annotation(sentry)")
    public Object publishMessage(ProceedingJoinPoint pjp, Sentry sentry) {
        return dev.soffa.foundation.commons.Sentry.get().watch(sentry.label(), new Supplier<Object>() {
            @SneakyThrows
            @Override
            public Object get() {
                return pjp.proceed(pjp.getArgs());
            }
        }, sentry.errorPropagation());
    }

}
