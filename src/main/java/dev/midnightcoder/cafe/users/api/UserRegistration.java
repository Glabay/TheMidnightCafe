package dev.midnightcoder.cafe.users.api;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-14
 */
public interface UserRegistration {
    UserDto register(RegistrationRequest request);
    UserDto registerOAuthUser(String identity);
    boolean existsByIdentity(String identity);
}
