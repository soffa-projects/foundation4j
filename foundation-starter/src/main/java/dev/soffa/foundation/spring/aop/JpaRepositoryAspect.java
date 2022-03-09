package dev.soffa.foundation.spring.aop;

import dev.soffa.foundation.errors.DatabaseException;
import dev.soffa.foundation.errors.ErrorUtil;
import dev.soffa.foundation.errors.ManagedException;
import dev.soffa.foundation.multitenancy.TenantHolder;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
public class JpaRepositoryAspect {

    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository*.*(..))")
    public Object catchJpaException(ProceedingJoinPoint jp) throws Throwable {
        final String currentTenant = TenantHolder.get().orElse(null);
        try {
            return jp.proceed();
        } catch (Exception e) {
            if (e instanceof ManagedException) {
                throw e;
            } else {
                Throwable error = ErrorUtil.unwrap(e);
                if (error instanceof ManagedException) {
                    throw error;
                }
                String msg = error.getMessage().toLowerCase();
                boolean hasMissingTable = msg.contains("table") && msg.contains("not found") || msg.contains("relation") && msg.contains("does not exist");
                if (hasMissingTable) {
                    throw new DatabaseException(error.getMessage() + " -- make sure the current tenant is valid (current: %s)", currentTenant, e);
                }
                throw new DatabaseException(error.getMessage(), e);
            }
        }
    }

}
