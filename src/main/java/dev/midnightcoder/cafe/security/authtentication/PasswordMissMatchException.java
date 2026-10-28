package dev.midnightcoder.cafe.security.authtentication;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-23
 */
public class PasswordMissMatchException extends RuntimeException {
    public PasswordMissMatchException() {
        super("Passwords do not match.");
    }
}
