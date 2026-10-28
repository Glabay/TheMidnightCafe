package dev.midnightcoder.cafe.security.authtentication.jwt.refresh;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * @author Glabay | Glabay-Studios
 * @project Midnight-Coder
 * @social Discord: Glabay
 * @since 2026-01-03
 */
@Component
public class SecureTokenGenerator {
    private final SecureRandom random = new SecureRandom();

    public String generate256Bit() {
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
