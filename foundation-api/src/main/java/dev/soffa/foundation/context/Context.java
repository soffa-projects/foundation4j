package dev.soffa.foundation.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.soffa.foundation.model.Authentication;
import dev.soffa.foundation.model.SideEffects;
import dev.soffa.foundation.model.TenantId;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@SuppressWarnings("PMD.GodClass")
public class Context {

    public static final String TENANT_ID = "X-TenantId";
    public static final String APPLICATION_ID = "X-ApplicationId";
    public static final String APPLICATION = "X-Application";
    // public static final String TRACE_ID = "X-TraceId";
    // public static final String SPAN_ID = "X-SpanId";
    public static final String SERVICE_NAME = "X-ServiceName";
    public static final String AUTHORIZATION = "Authorization";

    private static String serviceName = "app";
    private String authorization;
    private String tenantId;
    private String applicationId;
    private String applicationName;
    private String sender;
    // private String traceId;
    // private String spanId;


    @JsonIgnore
    private transient SideEffects sideEffects = new SideEffects();


    @JsonIgnore
    private transient Authentication authentication;

    public Context() {
        // this.traceId = UUID.randomUUID().toString();
        // this.spanId = UUID.randomUUID().toString();
        this.sender = serviceName;

    }

    public static Context create(String tenantId) {
        return new Context().withTenant(tenantId);
    }

    public static Context create() {
        return new Context();
    }

    @SneakyThrows
    public static void setServiceName(String value) {
        if (isEmpty(value)) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }
        serviceName = value;
    }

    @SneakyThrows
    public static Context fromHeaders(Map<String, String> headers) {
        Context context = new Context();
        if (headers == null) {
            return context;
        }
        for (Map.Entry<String, String> e : headers.entrySet()) {
            String value = e.getValue();
            if (isEmpty(value)) {
                continue;
            }
            String key = e.getKey();
            if (key.equalsIgnoreCase(Context.APPLICATION)) {
                context.setApplicationName(value);
            } else if (key.equalsIgnoreCase(Context.TENANT_ID)) {
                context.setTenantId(value);
            }/* else if (key.equalsIgnoreCase(Context.SPAN_ID)) {
                context.setTraceId(value);
            } else if (key.equalsIgnoreCase(Context.TRACE_ID)) {
                context.setSpanId(value);
            } */ else if (key.equalsIgnoreCase(Context.SERVICE_NAME)) {
                context.setSender(value);
            } else if (key.equalsIgnoreCase(Context.AUTHORIZATION)) {
                context.setAuthorization(value);
            }
        }
        return context;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.matches("\\s*");
    }

    private static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value;
    }

    public void sideEffect(String event) {
        sideEffects.of("service", serviceName).add(event);
    }

    public boolean hasAuthorization() {
        return isNotEmpty(authorization);
    }

    public String getSender() {
        return sender;
    }

    public String getServceName() {
        return sender;
    }

    public TenantId getTenant() {
        return TenantId.of(getTenantId());
    }

    public Context withTenant(String tenantId) {
        this.setTenantId(tenantId);
        return this;
    }

    public Context withBearer(String token) {
        return withAuthorization("Bearer " + token);
    }

    public Context withAuthorization(String authorization) {
        this.setAuthorization(authorization);
        return this;
    }

    public String getTenantId() {
        return trimToNull(tenantId);
    }

    public boolean hasTenant() {
        return isNotEmpty(tenantId);
    }

    public boolean hasApplicationId() {
        return isNotEmpty(applicationId);
    }

    public boolean isAuthenticated() {
        return authentication != null;
    }

    public void setAuthentication(Authentication auth) {
        this.authentication = auth;
        if (auth == null) {
            return;
        }
        tenantId = auth.getTenantId();
        if (auth.getApplication() != null && !auth.getApplication().isEmpty()) {
            applicationName = auth.getApplication();
        }
        applicationId = auth.getApplicationId();
    }

    public Optional<String> getUsername() {
        if (authentication != null) {
            return Optional.ofNullable(authentication.getUsername());
        }
        return Optional.empty();
    }

    public void sync() {
        this.sender = serviceName;
    }

    @SneakyThrows
    public Map<String, String> getContextMap() {
        Map<String, String> contextMap = new HashMap<>();
        if (isNotEmpty(getApplicationName())) {
            contextMap.put("application", getApplicationName());
        }
        if (hasTenant()) {
            contextMap.put("tenant", getTenantId());
        }
        if (hasApplicationId()) {
            contextMap.put("applicationId", getApplicationId());
        }
        /*
        if (isNotEmpty(getTraceId())) {
            contextMap.put("traceId", getTraceId());
        }
        if (isNotEmpty(getSpanId())) {
            contextMap.put("spanId", getSpanId());
        }

         */
        if (isNotEmpty(getSender())) {
            contextMap.put("sender", getSender());
        }
        if (getAuthentication() != null && isNotEmpty(getAuthentication().getUsername())) {
            contextMap.put("user", getAuthentication().getUsername());
        }
        contextMap.put("service_name", serviceName);
        return contextMap;
    }

    @SneakyThrows
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();

        if (isNotEmpty(getApplicationName())) {
            headers.put(Context.APPLICATION, getApplicationName());
        }
        if (hasTenant()) {
            headers.put(Context.TENANT_ID, getTenantId());
        }
        if (hasApplicationId()) {
            headers.put(Context.APPLICATION_ID, getApplicationId());
        }
        /*
        if (isNotEmpty(getTraceId())) {
            headers.put(Context.TRACE_ID, getTraceId());
        }
        if (isNotEmpty(getSpanId())) {
            headers.put(Context.SPAN_ID, getSpanId());
        }
         */
        if (isNotEmpty(getSender())) {
            headers.put(Context.SERVICE_NAME, getSender());
        }
        if (isNotEmpty(getAuthorization())) {
            headers.put(Context.AUTHORIZATION, getAuthorization());
        }
        return headers;
    }



}
