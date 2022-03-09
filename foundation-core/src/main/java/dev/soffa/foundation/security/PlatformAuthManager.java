package dev.soffa.foundation.security;

import dev.soffa.foundation.context.Context;

public interface PlatformAuthManager {

    void handle(Context context);

    void handle(Context context, String value);

}
