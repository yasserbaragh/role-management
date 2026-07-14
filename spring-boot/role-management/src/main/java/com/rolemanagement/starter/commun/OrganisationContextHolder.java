package com.rolemanagement.starter.commun;

public final class OrganisationContextHolder {

    private static final ThreadLocal<Long> CONTEXT = new ThreadLocal<>();

    private OrganisationContextHolder() {
    }

    public static void set(Long organisationId) {
        CONTEXT.set(organisationId);
    }

    public static Long get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Long getOrThrow() {
        Long id = CONTEXT.get();
        if (id == null) {
            throw new IllegalStateException("No organisation selected for this request");
        }
        return id;
    }
}
