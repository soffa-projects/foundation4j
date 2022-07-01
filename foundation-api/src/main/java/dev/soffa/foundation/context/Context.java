package dev.soffa.foundation.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.soffa.foundation.model.Authentication;
import dev.soffa.foundation.model.TenantId;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@SuppressWarnings("PMD.GodClass")
public class Context implements BaseContext {

    private static boolean production = true;

    public static final String TENANT_ID = "X-TenantId";
    public static final String TENANT_NAME = "X-TenantName";
    public static final String APPLICATION_ID = "X-ApplicationId";
    public static final String APPLICATION = "X-Application";
    // public static final String TRACE_ID = "X-TraceId";
    // public static final String SPAN_ID = "X-SpanId";
    public static final String SERVICE_NAME = "X-ServiceName";
    public static final String AUTHORIZATION = "Authorization";

    private static String serviceName = "app";
    @JsonIgnore
    private String authorization;
    private String tenantId;

    private boolean liveMode;

    private String accountId;
    private String accountName;
    private String tenantName;
    private String applicationId;
    private String ipAddress;
    private String applicationName;
    private String sender;

    @JsonIgnore
    private transient Authentication authentication;

    public Context() {
        this.sender = serviceName;
    }

    public static void setProduction(boolean production) {
        Context.production = production;
    }

    @Override
    public boolean isProduction() {
        return Context.production;
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
            } else if (key.equalsIgnoreCase(Context.TENANT_NAME)) {
                context.setTenantName(value);
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

    @Override
    public boolean hasAuthorization() {
        return isNotEmpty(authorization);
    }

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public String getServiceName() {
        return sender;
    }

    @Override
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

    @Override
    public String getTenantId() {
        return trimToNull(tenantId);
    }

    @Override
    public boolean hasTenant() {
        return isNotEmpty(tenantId);
    }

    @Override
    public boolean hasApplicationId() {
        return isNotEmpty(applicationId);
    }

    @Override
    public boolean hasAccountId() {
        return isNotEmpty(accountId);
    }

    @Override
    public boolean hasIpAddress() {
        return isNotEmpty(ipAddress);
    }

    @Override
    public boolean isAuthenticated() {
        return authentication != null;
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication auth) {
        this.authentication = auth;
        if (auth == null) {
            return;
        }

        tenantId = auth.getTenantId();
        tenantName = auth.getTenantName();

        if (auth.getApplication() != null && !auth.getApplication().isEmpty()) {
            applicationName = auth.getApplication();
        }
        applicationId = auth.getApplicationId();
        accountId = auth.getAccountId();
        liveMode = auth.isLiveMode();
        accountName = auth.getAccountName();
    }

    @Override
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
    @JsonIgnore
    public Map<String, String> getContextMap() {
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("account_id", getAccountId());
        contextMap.put("account_name", getAccountName());
        contextMap.put("application_name", getApplicationName());
        contextMap.put("application_id", getApplicationId());
        contextMap.put("tenant", getTenantId());
        contextMap.put("tenant_id", getTenantId());
        contextMap.put("tenant_name", getTenantName());
        contextMap.put("live_mode", isLiveMode() ? "true" : "false");
        contextMap.put("ip_address", getApplicationId());
        contextMap.put("sender", getSender());
        contextMap.put("env", "production");
        if (getAuthentication() != null) {
            contextMap.put("username", getAuthentication().getUsername());
            contextMap.put("user_id", getAuthentication().getUserId());
        }
        contextMap.put("service_name", serviceName);
        contextMap.values().removeAll(Collections.singleton(null));
        return contextMap;
    }

    @SneakyThrows
    @JsonIgnore
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
        if (isNotEmpty(getSender())) {
            headers.put(Context.SERVICE_NAME, getSender());
        }
        if (isNotEmpty(getAuthorization())) {
            headers.put(Context.AUTHORIZATION, getAuthorization());
        }
        return headers;
    }


}
