package dev.midnightcoder.cafe.security.autherization;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-24
 */
public enum MidnightPermissions {
    CUSTOMER_READ("permission:customer:read"),
    CUSTOMER_WRITE("permission:customer:write"),
    CUSTOMER_EDIT("permission:customer:edit"),
    CUSTOMER_CREATE("permission:customer:create"),

    EMPLOYEE_READ("permission:employee:read"),
    EMPLOYEE_WRITE("permission:employee:write"),
    EMPLOYEE_EDIT("permission:employee:edit"),
    EMPLOYEE_CREATE("permission:employee:create"),

    PRODUCT_READ("permission:product:read"),
    PRODUCT_WRITE("permission:product:write"),
    PRODUCT_EDIT("permission:product:edit"),
    PRODUCT_CREATE("permission:product:create");

    private final String authority;

    MidnightPermissions(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
