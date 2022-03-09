package dev.soffa.foundation.multitenancy;

import java.util.HashSet;
import java.util.Set;

public interface TenantsLoader {

    default Set<String> getTenantList() {
        return new HashSet<>();
    }

}
