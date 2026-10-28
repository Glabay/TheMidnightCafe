package dev.midnightcoder.cafe.security.autherization;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-24
 */
public enum MidnightRoles {
    ROLE_EMPLOYEE("Barista"),
    ROLE_BARISTA("Head Barista"),
    ROLE_HEAD_BARISTA("Pastry Chef"),
    ROLE_PASTRY_CHEF("Baker"),
    ROLE_SHIFT_LEAD("Cashier"),
    ROLE_SUPERVISOR("Shift Supervisor"),
    ROLE_MANAGER("Store Manager"),
    ROLE_ADMIN("Assistant Manager");

    private final String roleTitle;

    MidnightRoles(String roleTitle) {
        this.roleTitle = roleTitle;
    }

    public String getRoleTitle() {
        return roleTitle;
    }
}
