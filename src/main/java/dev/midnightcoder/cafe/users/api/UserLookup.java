package dev.midnightcoder.cafe.users.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-11
 */
public interface UserLookup {
    Optional<UserDto> findByIdentity(String identity);
    Optional<UserDto> findByUserId(UUID userId);
    List<UserDto> findAll();
}
