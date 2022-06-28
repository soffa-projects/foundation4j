package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.model.TenantId;

public interface TenantDelegate {

    default TenantId getTenant(Context context) {
        return TenantId.CONTEXT;
    }
}
