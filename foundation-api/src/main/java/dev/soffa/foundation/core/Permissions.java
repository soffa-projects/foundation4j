package dev.soffa.foundation.core;

public interface Permissions {

    String IS_ROOT = "root";
    String IS_ADMIN = "admin";
    String IS_USER = "user";
    String IS_SERVICE = "service";
    String IS_APPLICATION = "application";
    String IS_AUTHENTICATED = "authenticated";

    String HAS_USER_PROFILE = "has_user_profile";
    String HAS_APPLICATION = "has_application";
    String HAS_TENANT = "has_tenant";
}
