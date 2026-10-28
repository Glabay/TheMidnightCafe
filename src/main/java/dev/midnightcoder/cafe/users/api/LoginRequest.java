package dev.midnightcoder.cafe.users.api;

import jakarta.validation.constraints.NotBlank;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-14
 */
public record LoginRequest(
    @NotBlank(message = "Identity cannot be empty")
    String identity,
    @NotBlank(message = "Password cannot be empty")
    String password
) {
    public String username() {
        return identity;
    }
}
