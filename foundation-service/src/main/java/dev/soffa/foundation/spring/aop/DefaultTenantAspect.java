package dev.soffa.foundation.spring.aop;

import dev.soffa.foundation.annotations.DefaultTenant;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.multitenancy.TenantHolder;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Aspect
@Component
@AllArgsConstructor
public class DefaultTenantAspect {

    public static final Logger LOG = Logger.get(DefaultTenantAspect.class);

    @SneakyThrows
    @Around("@annotation(context)")
    public Object handleOperation(ProceedingJoinPoint pjp, DefaultTenant context) {
        return TenantHolder.useDefault(new Supplier<Object>() {
            @SneakyThrows
            @Override
            @Transactional(propagation = Propagation.NESTED)
            public Object get() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using default tenant for action: %s.%S", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName());
                }
                return pjp.proceed(pjp.getArgs());
            }
        });
    }


}
