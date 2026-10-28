package dev.midnightcoder.cafe.security.autherization;

import module java.base;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-24
 */
public final class DefaultRolePermissions {

    private DefaultRolePermissions() {}

    public static final Map<MidnightRoles, Set<MidnightPermissions>> DEFAULTS = Map.of(

        MidnightRoles.ROLE_EMPLOYEE,
        Set.of(
            MidnightPermissions.CUSTOMER_READ,
            MidnightPermissions.PRODUCT_READ,
            MidnightPermissions.PRODUCT_EDIT
        ),

        MidnightRoles.ROLE_BARISTA,
        Set.of(
            MidnightPermissions.CUSTOMER_READ,
            MidnightPermissions.PRODUCT_READ,
            MidnightPermissions.PRODUCT_EDIT
        ),

        MidnightRoles.ROLE_HEAD_BARISTA,
        Set.of(
            MidnightPermissions.CUSTOMER_READ,
            MidnightPermissions.CUSTOMER_EDIT,
            MidnightPermissions.PRODUCT_READ,
            MidnightPermissions.PRODUCT_EDIT
        ),

        MidnightRoles.ROLE_MANAGER,
        Set.of(
            MidnightPermissions.CUSTOMER_READ,
            MidnightPermissions.CUSTOMER_WRITE,
            MidnightPermissions.CUSTOMER_EDIT,
            MidnightPermissions.CUSTOMER_CREATE,

            MidnightPermissions.EMPLOYEE_READ,
            MidnightPermissions.EMPLOYEE_EDIT,

            MidnightPermissions.PRODUCT_READ,
            MidnightPermissions.PRODUCT_WRITE,
            MidnightPermissions.PRODUCT_EDIT,
            MidnightPermissions.PRODUCT_CREATE
        )
    );
}