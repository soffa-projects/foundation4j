package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.error.ErrorUtil;
import dev.soffa.foundation.error.FunctionalException;
import io.opentelemetry.api.trace.Span;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = Logger.get(CustomRestExceptionHandler.class);
    private final Environment environment;

    @Autowired
    public CustomRestExceptionHandler(Environment environment) {
        super();
        this.environment = environment;
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatus status,
                                                                  @NonNull WebRequest request) {
        return handleGlobalErrors(ex, request);
    }

    @ExceptionHandler({Throwable.class, Exception.class})
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public ResponseEntity<Object> handleGlobalErrors(Throwable ex, WebRequest request) {

        String accept = request.getHeader("accept");

        if (TextUtil.isNotEmpty(accept) && accept.contains("octet-stream")) {
            Logger.app.error(ErrorUtil.loookupOriginalMessage(ex));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorUtil.loookupOriginalMessage(ex), ex);
        }

        boolean isProduction = environment.acceptsProfiles(Profiles.of("prod", "production"));
        Throwable error = ErrorUtil.unwrap(ex);
        HttpStatus status = deriveStatus(error);
        String message = ErrorUtil.loookupOriginalMessage(error);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("source", environment.getRequiredProperty("spring.application.name"));
        body.put("kind", error.getClass().getSimpleName());
        body.put("status", status.value());
        body.put("message", message);
        body.put("prod", isProduction);
        ContextHolder.get().ifPresent(context -> {
            body.put("livemode", context.isLiveMode());
        });
        if (!isProduction) {
            Optional.ofNullable(SecurityContextHolder.getContext()).ifPresent(context -> {
                Authentication authentication = context.getAuthentication();
                if (authentication != null && authentication.getAuthorities() != null) {
                    body.put("permissions", authentication.getAuthorities().stream().map(Object::toString).collect(Collectors.joining(",")));
                }
            });
        }

        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        String auth = ((ServletWebRequest) request).getRequest().getHeader("Authorization");
        if (TextUtil.isNotEmpty(auth)) {
            body.put("authorization", "*****" + TextUtil.takeLast(auth, 4));
        }
        Span span = Span.current();

        if (span != null && span.getSpanContext().getTraceId() != null &&
            !span.getSpanContext().getTraceId().startsWith("0000000000000000")) {
            body.put("traceId", span.getSpanContext().getTraceId());
            body.put("spanId", span.getSpanContext().getSpanId());
        }

        ContextHolder.get().ifPresent(context -> {
            Optional.ofNullable(context.getApplicationName()).ifPresent(s -> body.put("application", s));
            context.getUsername().ifPresent(s -> body.put("user", s));
            if (context.hasTenant()) {
                body.put("tenant", context.getTenantId());
            }
        });

        if (status.value() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            LOG.error(error);
        } else {
            LOG.error(error.getMessage());
        }
        if (!isProduction && status != HttpStatus.UNAUTHORIZED && status != HttpStatus.FORBIDDEN) {
            body.put("trace", ErrorUtil.getStacktrace(error).split("\n"));
        }

        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus deriveStatus(Throwable exception) {
        int code = ErrorUtil.resolveErrorCode(exception);
        if (code > -1) {
            return HttpStatus.valueOf(code);
        }

        if (exception instanceof AccessDeniedException) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                return HttpStatus.FORBIDDEN;
            }
            return HttpStatus.UNAUTHORIZED;
        }
        if (exception instanceof MethodArgumentNotValidException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exception instanceof FunctionalException) {
            return HttpStatus.NOT_IMPLEMENTED;
        }
        if (exception instanceof ResponseStatusException) {
            return ((ResponseStatusException) exception).getStatus();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }


}
