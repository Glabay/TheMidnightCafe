package dev.midnightcoder.cafe.security.authtentication.jwt.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Glabay | Glabay-Studios
 * @project Midnight-Coder
 * @social Discord: Glabay
 * @since 2026-03-23
 */
@Component
public class RefreshTokenCleanupTask {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupTask.class);
    private final RefreshTokenService refreshTokenService;

    public RefreshTokenCleanupTask(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    /// Runs daily at 3 AM
    @Scheduled(cron = "0 0 3 * * *")
    public void performCleanup() {
        var removed = refreshTokenService.cleanupExpired();
        log.info("Removed {} expired refresh tokens", removed);
    }
}
