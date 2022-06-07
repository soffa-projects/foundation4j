package dev.soffa.foundation.spring.aop;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.error.UnauthorizedException;
import dev.soffa.foundation.error.ValidationException;
import dev.soffa.foundation.multitenancy.TenantHolder;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class SecurityAspect {

    private static final Logger LOG = Logger.get(SecurityAspect.class);
    private static final Throwable ERR_AUTH_REQUIRED = new UnauthorizedException("Authentication is required to access this resource.");
    private static final Throwable ERR_APP_REQUIRED = new ValidationException("An ApplicationName is required to access this resource.");
    private static final Throwable ERR_TENANT_REQUIRED = new ValidationException("A TenantId is required to access this resource.");

    @SneakyThrows
    @Before("@within(dev.soffa.foundation.annotation.Authenticated) || @annotation(dev.soffa.foundation.annotation.Authenticated)")
    public void checkAuthenticated(JoinPoint point) {
        LOG.debug("[aspect] Checking authentication...");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth==null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            LOG.warn("Access denied to [%s.%s], current context does not contain an authentication", point.getSignature().getDeclaringTypeName(), point.getSignature().getName());
            throw ERR_AUTH_REQUIRED;
        }
    }

    @SneakyThrows
    @Before("@within(dev.soffa.foundation.annotation.ApplicationRequired) || @annotation(dev.soffa.foundation.annotation.ApplicationRequired)")
    public void checkApplication(JoinPoint point) {
        LOG.debug("[aspect] Checking application...");
        Context context = getRequestContext().orElseThrow(() -> ERR_APP_REQUIRED);
        if (TextUtil.isEmpty(context.getApplicationName())) {
            LOG.warn("Access denied to [%s.%s], current context does not contain a valid applicationName", point.getSignature().getDeclaringTypeName(), point.getSignature().getName());
            throw ERR_APP_REQUIRED;
        }
    }

    @SneakyThrows
    @Before("@within(dev.soffa.foundation.annotation.TenantRequired) || @annotation(dev.soffa.foundation.annotation.TenantRequired)")
    public void checkTenant(JoinPoint point) {
        LOG.debug("Enforcing TenantRequired");
        if (TenantHolder.isEmpty() || TenantHolder.isDefault()) {
            LOG.warn("Access denied to [%s.%s], current context does not contain a valid tenant", point.getSignature().getDeclaringTypeName(), point.getSignature().getName());
            throw ERR_TENANT_REQUIRED;
        }
    }

    private Optional<Context> getRequestContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null && auth.getPrincipal() instanceof Context) {
            return Optional.of((Context) auth.getPrincipal());
        }
        return Optional.empty();
    }

}
