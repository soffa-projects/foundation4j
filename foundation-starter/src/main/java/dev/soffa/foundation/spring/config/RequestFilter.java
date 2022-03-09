package dev.soffa.foundation.spring.config;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.*;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.errors.ErrorUtil;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.security.PlatformAuthManager;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@NoArgsConstructor
public class RequestFilter extends OncePerRequestFilter {

    private static final Logger LOG = Logger.get(RequestFilter.class);
    private PlatformAuthManager authManager;

    public RequestFilter(PlatformAuthManager authManager) {
        super();
        this.authManager = authManager;
        // this.metricsRegistry = metricsRegistry;
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("===================== Serving request: %s %s =====================", request.getMethod(), request.getRequestURI());
        }

        Context context = new Context();

        lookupHeader(request, "X-TenantId", "X-Tenant").ifPresent(value -> {
            LOG.debug("Tenant found in header", value);
            context.setTenantId(value);
            TenantHolder.set(value);
        });
        lookupHeader(request, "X-Application", "X-ApplicationName", "X-ApplicationId", "X-App").ifPresent(context::setApplicationName);
        lookupHeader(request, "X-TraceId", "X-Trace-Id").ifPresent(context::setTraceId);
        lookupHeader(request, "traceparent").ifPresent(context::setTraceId);
        //lookupHeader(request, "X-SpanId", "X-Span-Id", "X-CorrelationId", "X-Correlation-Id").ifPresent(context::setSpanId);

        LOG.debug("Pre-setting context with tracing data");

        processTracing(context);
        ContextHolder.set(context);

        AtomicBoolean proceed = new AtomicBoolean(true);

        LOG.debug("Looking up authorization");
        //noinspection Convert2Lambda
        lookupHeader(request, HttpHeaders.AUTHORIZATION, "X-JWT-Assertion", "X-JWT-Assertions").ifPresent(new Consumer<String>() {
            @SneakyThrows
            @Override
            public void accept(String value) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authorization header found, fingerpint: %s", DigestUtil.md5(value));
                }

                try {
                    authManager.handle(context, value);
                } catch (Exception e) {
                    proceed.set(false);
                    int statusCode = ErrorUtil.resolveErrorCode(e);
                    if (statusCode > -1) {
                        response.setContentType("application/json");
                        response.sendError(statusCode, Mappers.JSON.serialize(ImmutableMap.of(
                            "message", e.getMessage()
                        )));
                    } else if (e instanceof AccessDeniedException) {
                        response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
                    } else {
                        LOG.error(e);
                        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
                    }
                }
            }
        });

        if (!proceed.get()) {
            LOG.debug("Aborting current request");
            return;
        }

        try {
            LOG.debug("Setting request context and tenant before proceeding");
            ContextHolder.set(context);
            TenantHolder.set(context.getTenantId());
            chain.doFilter(request, response);
        } finally {
            if (LOG.isDebugEnabled() && context.getSideEffects() != null && !context.getSideEffects().isEmpty()) {
                LOG.debug("SIDE_EFFECTS");
                LOG.debug(Mappers.JSON.prettyPrint(context.getSideEffects()));
            }
            ContextHolder.clear();
            TenantHolder.clear();
        }
    }

    private void processTracing(Context context) {
        String prefix = "";
        if (context.getTenantId() != null) {
            prefix = context.getTenantId() + "_";
            Logger.setTenantId(context.getTenantId());
        }

        if (TextUtil.isEmpty(context.getSpanId())) {
            context.setSpanId(IdGenerator.shortUUID(prefix));
        }
        if (TextUtil.isEmpty(context.getTraceId())) {
            context.setTraceId(IdGenerator.shortUUID(prefix));
        }
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = "/" + request.getRequestURI().split("\\?")[0].replaceAll("^/|/$", "".toLowerCase());
        uri = uri.replace(request.getContextPath(), "");
        if (!uri.startsWith("/")) {
            uri = "/";
        }
        boolean isStaticResourceRequest = uri.matches(".*\\.(css|js|ts|html|htm|map|g?zip|gz|ico|png|gif|svg|woff|ttf|eot|jpe?g2?)$");
        if (isStaticResourceRequest) {
            return true;
        }
        boolean isOpenAPIRequest = uri.matches("/swagger.*") || uri.matches("/v3/api-docs/?.*?");
        if (isOpenAPIRequest) {
            return true;
        }
        return uri.matches("/actuator/.*|/healthz");
    }

    private Optional<String> lookupHeader(HttpServletRequest request, String... candidates) {
        for (String candidate : candidates) {
            String header = request.getHeader(candidate);
            if (TextUtil.isNotEmpty(header)) {
                return Optional.of(header.trim());
            }
        }
        return Optional.empty();
    }

}
