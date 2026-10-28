package dev.midnightcoder.cafe.security.authtentication;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-14
 */
public record LoginRequest(
    String identity,
    String password
) {
    public LoginRequest {
        if (identity == null || identity.isBlank()) {
            throw new IllegalArgumentException("Identity cannot be empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }

    public String username() {
        return identity;
    }
}
