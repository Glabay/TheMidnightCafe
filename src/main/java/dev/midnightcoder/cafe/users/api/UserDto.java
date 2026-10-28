package dev.midnightcoder.cafe.users.api;

import dev.midnightcoder.cafe.security.autherization.MidnightRoles;
import dev.midnightcoder.cafe.security.autherization.Role;

import java.util.Set;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-11
 */
public record UserDto(
    UUID id,
    String identity,
    String passwordHash,
    Boolean enabled,
    Set<Role> roles
) {
    public UserDto(String identity, String passwordHash) {
        this(null, identity, passwordHash, true, Set.of());
    }

    public UserDto(String identity, String passwordHash, Boolean enabled) {
        this(null, identity, passwordHash, enabled, Set.of());
    }
}
