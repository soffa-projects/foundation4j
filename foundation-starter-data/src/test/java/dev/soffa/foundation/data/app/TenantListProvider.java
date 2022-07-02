package dev.soffa.foundation.data.app;

import com.google.common.collect.ImmutableSet;
import dev.soffa.foundation.multitenancy.TenantsLoader;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TenantListProvider implements TenantsLoader {

    @Override
    public Set<String> getTenantList() {
        return ImmutableSet.of("t1", "t2", "t3");
    }
}
