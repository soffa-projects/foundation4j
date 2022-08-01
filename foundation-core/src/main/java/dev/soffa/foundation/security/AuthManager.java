package dev.soffa.foundation.security;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.model.Authentication;

public interface AuthManager {

    default Authentication authenticate(Context context) {
        return authenticate(context, context.getAuthorization());
    }

    default Authentication authenticate(Context context, String token) {
        return null;
    }

    default Authentication authenticate(Context context, String username, String password) {
        return null;
    }
}
