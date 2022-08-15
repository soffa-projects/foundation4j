package dev.soffa.foundation.context;

import dev.soffa.foundation.model.Authentication;
import dev.soffa.foundation.model.TenantId;

import java.util.Optional;

public interface BaseContext {
    boolean isProduction();

    boolean isLiveMode();

    boolean hasAuthorization();

    String getSender();

    String getServiceName();

    TenantId getTenant();

    String getTenantId();

    boolean hasTenant();

    boolean hasApplicationId();

    boolean hasAccountId();

    boolean hasIpAddress();

    boolean isAuthenticated();

    Authentication getAuthentication();

    Optional<String> getUsername();

    Optional<String> getUserEmail();

    String getAccountId();

    String getAccountName();

    String getTenantName();

    String getApplicationId();

    String getIpAddress();

    String getApplicationName();
}
