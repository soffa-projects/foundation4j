package dev.soffa.foundation.multitenancy;

import java.util.Set;

public interface TenantsLoader {

    TenantsLoader NOOP = () -> null;

    Set<String> getTenantList();

}
