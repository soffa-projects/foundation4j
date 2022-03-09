package dev.soffa.foundation.multitenancy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.config.AppConfig;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.messages.pubsub.PubSubClientFactory;
import dev.soffa.foundation.messages.pubsub.PubSubMessenger;
import dev.soffa.foundation.models.TenantList;
import dev.soffa.foundation.models.Token;
import dev.soffa.foundation.models.TokenType;
import dev.soffa.foundation.security.TokenProvider;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class PubSubTenantsLoader implements TenantsLoader {

    private static final Logger LOG = Logger.get(TenantsLoader.class);

    private final PubSubMessenger client;
    private final TokenProvider tokens;
    private final AppConfig app;
    private final String serviceId;
    private final String permissions;

    @Override
    public Set<String> getTenantList() {
        GetTenantList operation = PubSubClientFactory.of(GetTenantList.class, serviceId, client);
        try {
            Token token = tokens.create(TokenType.JWT, app.getName(), ImmutableMap.of("permissions", permissions));
            Context context = new Context().withAuthorization("Bearer " + token.getValue());
            TenantList res = operation.handle(null, context);
            if (res == null || res.getTenants() == null) {
                LOG.warn("Call to service %s returned an empty tenants list.", serviceId);
                return ImmutableSet.of();
            }
            LOG.warn("Call to service %s returned %d tenant(s).", serviceId, res.getTenants().size());
            return res.getTenants();
        } catch (Exception e) {
            LOG.error("Unable to fetch tenants list from bantu-accounts, make sure the service is reachable", e);
            return ImmutableSet.of();
        }
    }
}
