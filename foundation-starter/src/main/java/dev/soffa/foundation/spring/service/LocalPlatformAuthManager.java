package dev.soffa.foundation.spring.service;

import com.google.common.collect.ImmutableSet;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Permissions;
import dev.soffa.foundation.model.Authentication;
import dev.soffa.foundation.security.AuthManager;
import dev.soffa.foundation.security.PlatformAuthManager;
import dev.soffa.foundation.security.TokenProvider;
import dev.soffa.foundation.spring.config.NoopAuthManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class LocalPlatformAuthManager implements PlatformAuthManager {

    private static final Logger LOG = Logger.get(PlatformAuthManager.class);
    private final TokenProvider tokens;
    //private final AuthManager authManger;

    private final ApplicationContext context;

    public LocalPlatformAuthManager(ApplicationContext context, @Autowired(required = false) TokenProvider tokens) {
        this.tokens = tokens;
        this.context = context;
        // this.authManger = authManger;
    }

    private AuthManager getAuthManager() {
        String[] beans = context.getBeanNamesForType(AuthManager.class);
        if (beans.length == 0) {
            return new NoopAuthManager();
        }
        return context.getBean(AuthManager.class); // Cache this;
    }

    private Authentication authenticate(Context context, String token) {
        if (TextUtil.isEmpty(token)) {
            LOG.warn("An empty authentication was received, check the client request");
            return null;
        }
        Authentication auth = getAuthManager().authenticate(context, token);
        if (auth != null) {
            LOG.info("Authentication provided by local %s", getAuthManager().getClass().getName());
            return auth;
        }
        if (tokens == null) {
            LOG.info("No tokensProvider available, retuning empty authentication");
            return null;
        }

        return tokens.decode(token);
    }

    private Authentication authenticate(Context context, String username, String password) {
        return getAuthManager().authenticate(context, username, password);
    }

    @Override
    public void handle(Context context) {
        handle(context, context.getAuthorization());
    }

    @Override
    public void handle(Context context, String token) {

        if (TextUtil.isEmpty(token)) {
            return;
        }
        Authentication auth = null;

        if (token.toLowerCase().startsWith("bearer ")) {
            String lToken = token.substring("bearer ".length()).trim();
            LOG.debug("Bearer authorization header received");
            auth = authenticate(context, lToken);
        } else if (token.toLowerCase().startsWith("basic ")) {
            LOG.debug("Basic authorization header received");
            String basicAuth = token.substring("basic ".length()).trim();
            String[] credentials = new String(Base64.getDecoder().decode(basicAuth)).split(":");
            boolean isValid = credentials.length >= 1;
            if (isValid) {
                String username = credentials[0];
                boolean hasPassword = credentials.length > 1;
                String pasword = hasPassword ? credentials[1] : "";
                if (tokens != null && pasword.equals(tokens.getConfig().getSecret())) {
                    auth = Authentication.builder()
                        .application(username)
                        //.username(username)
                        .tenantId(context.getTenantId())
                        .principal(username)
                        .permissions(ImmutableSet.of(Permissions.IS_SERVICE))
                        .roles(ImmutableSet.of(Permissions.IS_SERVICE))
                        .build();
                } else {
                    auth = authenticate(context, username, pasword);
                }
            }
        } else {
            LOG.warn("An authorization header was found but it is not a bearer or basic authorization header");
        }

        if (auth == null) {
            return;
        }

        context.setAuthentication(auth);
        context.setAuthorization(token);
        List<GrantedAuthority> permissions = createPermissions(context, auth);
        UsernamePasswordAuthenticationToken authz = new UsernamePasswordAuthenticationToken(context, null, permissions);
        SecurityContextHolder.getContext().setAuthentication(authz);
    }

    private List<GrantedAuthority> createPermissions(Context context, Authentication auth) {
        List<GrantedAuthority> permissions = new ArrayList<>();
        permissions.add(new SimpleGrantedAuthority(Permissions.IS_AUTHENTICATED));
        if (auth.getProfile() != null) {
            permissions.add(new SimpleGrantedAuthority(Permissions.IS_USER));
            permissions.add(new SimpleGrantedAuthority(Permissions.HAS_USER_PROFILE));
        } else {
            permissions.add(new SimpleGrantedAuthority(Permissions.IS_APPLICATION));
        }
        if (TextUtil.isNotEmpty(context.getApplicationName())) {
            permissions.add(new SimpleGrantedAuthority(Permissions.HAS_APPLICATION));
        }
        if (context.getTenantId() != null) {
            permissions.add(new SimpleGrantedAuthority(Permissions.HAS_TENANT));
        }
        if (auth.getRoles() != null) {
            for (String role : auth.getRoles()) {
                if (TextUtil.isNotEmpty(role)) {
                    permissions.add(new SimpleGrantedAuthority(role.trim()));
                }
            }
        }
        if (auth.getPermissions() != null) {
            for (String permission : auth.getPermissions()) {
                if (TextUtil.isNotEmpty(permission)) {
                    permissions.add(new SimpleGrantedAuthority(permission.trim()));
                }
            }
        }
        return permissions;
    }

}
