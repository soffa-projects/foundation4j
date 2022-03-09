package dev.soffa.foundation.spring.aop;

import dev.soffa.foundation.annotations.LogContext;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TemplateHelper;
import dev.soffa.foundation.commons.TextUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Aspect
@Component
@AllArgsConstructor
public class LogContextAspect {

    public static final Logger LOG = Logger.get(LogContextAspect.class);

    @SneakyThrows
    @Around("@annotation(context)")
    public Object handleOperation(ProceedingJoinPoint pjp, LogContext context) {
        Map<String, String> values = new HashMap<>();

        //EvaluationContext pcontext  = new StandardEvaluationContext(pjp.getArgs());
        Map<String, Object> root = new HashMap<>(); // ImmutableMap.of(pjp.getArgs());
        if (pjp.getArgs() != null) {
            for (int i = 0; i < pjp.getArgs().length; i++) {
                root.put("arg" + i, pjp.getArgs()[i]);
            }
        }

        for (String expression : context.value()) {
            if (!expression.contains("=")) {
                LOG.warn("An invalid context expression was provided (= missing): %s // %s", expression, pjp.getSignature().getName());
                continue;
            }
            String[] parts = expression.split("=");
            if (TextUtil.isEmpty(parts[1])) {
                LOG.warn("An invalid context expression was provided (missing value after '='): %s // %s", expression, pjp.getSignature().getName());
                continue;
            }
            Object value = TemplateHelper.render(expression, root);
            if (value == null) {
                LOG.warn("Parsed expression is null, kipping -- %s // %s", expression, pjp.getSignature().getName());
                continue;
            }
            values.put(parts[0], value.toString());
        }
        //noinspection Convert2Lambda
        return Logger.withContext(values, new Supplier<Object>() {
            @SneakyThrows
            @Override
            public Object get() {
                return pjp.proceed(pjp.getArgs());
            }
        });
    }


}
