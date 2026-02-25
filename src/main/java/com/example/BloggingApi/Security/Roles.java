package com.example.BloggingApi.Security;

/**
 * Application roles for RBAC. Used with @PreAuthorize and in SecurityConfig.
 */
public final class Roles {

    public static final String ADMIN = "ADMIN";
    public static final String AUTHOR = "AUTHOR";
    public static final String READER = "READER";

    private Roles() {}
}
