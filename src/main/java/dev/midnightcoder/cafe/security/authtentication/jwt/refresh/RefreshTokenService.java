package dev.midnightcoder.cafe.security.authtentication.jwt.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Glabay | Glabay-Studios
 * @project Midnight-Coder
 * @social Discord: Glabay
 * @since 2026-01-03
 */
@Service
public class RefreshTokenService {
    private final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository repository;
    private final SecureTokenGenerator tokenGenerator;
    private final TokenHashingService hashingService;

    @Value("${jwt.refresh-ttl-days}")
    private int refreshTtlDays;

    @Value("${security.refresh.detect-reuse}")
    private boolean detectReuse;

    @Value("${security.refresh.revoke-family-on-reuse}")
    private boolean revokeFamilyOnReuse;

    @Value("${security.refresh.cleanup-expired}")
    private boolean cleanupExpired;

    @Value("${security.refresh.cleanup-grace-days}")
    private int cleanupGraceDays;

    public RefreshTokenService(RefreshTokenRepository repository, SecureTokenGenerator tokenGenerator, TokenHashingService hashingService) {
        this.repository = repository;
        this.tokenGenerator = tokenGenerator;
        this.hashingService = hashingService;
    }

    @Transactional
    public IssueResult issue(String userEmail, String userAgent, String ip) {
        // issuing a new RT -> revoke old ones...
        repository.findAllByUserEmailAndRevokedAtIsNull(userEmail)
            .forEach(t -> revokeFamily(t.getId()));
        // create some data
        var now = LocalDateTime.now();
        var expiresAt = now.plusDays(refreshTtlDays);
        var raw = tokenGenerator.generate256Bit();
        var hash = hashingService.hash(raw);
        // Build a new RT
        var entity = new RefreshToken();
            entity.setId(UUID.randomUUID());
            entity.setUserEmail(userEmail);
            entity.setTokenHash(hash);
            entity.setIssuedAt(now);
            entity.setExpiresAt(expiresAt);
            entity.setUserAgent(userAgent);
            entity.setIp(ip);
        // Cache the token to the database
        repository.save(entity);
        return new IssueResult(entity.getId(), raw, expiresAt);
    }

    @Transactional(readOnly = true)
    public Optional<ActiveToken> validateActive(String rawToken) {
        var hash = hashingService.hash(rawToken);
        var found = repository.findByTokenHashAndRevokedAtIsNull(hash);
        if (found.isEmpty())
            return Optional.empty();
        var token = found.get();
        if (token.getExpiresAt().isBefore(LocalDateTime.now()))
            return Optional.empty();
        return Optional.of(new ActiveToken(token.getId(), token.getUserEmail(), token.getExpiresAt()));
    }

    @Transactional
    public RotateResult rotate(String rawToken, String userAgent, String ip) {
        var hash = hashingService.hash(rawToken);
        var opt = repository.findByTokenHash(hash);
        if (opt.isEmpty())
            return RotateResult.invalid();
        var current = opt.get();
        var now = LocalDateTime.now();

        if (current.getRevokedAt() != null) {
            if (detectReuse) {
                log.warn("Refresh token reuse detected for user {} starting from token {}", current.getUserEmail(), current.getId());
                if (revokeFamilyOnReuse)
                    revokeFamilyFrom(current.getId());
            }
            return RotateResult.reused();
        }

        if (current.getExpiresAt().isBefore(now)) {
            // expired
            current.setRevokedAt(now);
            repository.save(current);
            return RotateResult.expired();
        }

        // Create a new token and link it
        var issued = issue(current.getUserEmail(), userAgent, ip);

        // revoke current and link to new id
        current.setRevokedAt(now);
        current.setReplacedBy(issued.tokenId());
        repository.save(current);

        return RotateResult.rotated(current.getUserEmail(), issued.tokenId(), issued.rawToken(), issued.expiresAt());
    }

    @Transactional
    public boolean revokeCurrent(String rawToken) {
        var hash = hashingService.hash(rawToken);
        var opt = repository.findByTokenHash(hash);
        if (opt.isEmpty()) return false;
        var token = opt.get();
        if (token.getRevokedAt() == null) {
            token.setRevokedAt(LocalDateTime.now());
            repository.save(token);
            return true;
        }
        return false;
    }

    @Transactional
    public int revokeFamily(UUID startId) {
        return revokeFamilyFrom(startId);
    }

    private int revokeFamilyFrom(UUID startId) {
        var count = 0;
        var cursor = startId;
        var now = LocalDateTime.now();
        while (cursor != null) {
            var opt = repository.findById(cursor);
            if (opt.isEmpty()) break;
            var token = opt.get();
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(now);
                repository.save(token);
                count++;
            }
            cursor = token.getReplacedBy();
        }
        return count;
    }

    @Transactional
    public int cleanupExpired() {
        if (!cleanupExpired) return 0;
        var threshold = LocalDateTime.now().minusDays(cleanupGraceDays);
        int removed = 0;
        for (var t : repository.findAllExpired(threshold)) {
            try {
                repository.delete(t);
                removed++;
            } catch (Exception _) {}
        }
        return removed;
    }

    public record IssueResult(UUID tokenId, String rawToken, LocalDateTime expiresAt) {}

    public static final class RotateResult {
        public final boolean success;
        public final boolean invalid;
        public final boolean reused;
        public final boolean expired;
        public final String userEmail;
        public final UUID newTokenId;
        public final String newRawToken;
        public final LocalDateTime newExpiresAt;

        private RotateResult(boolean success,
                             boolean invalid,
                             boolean reused,
                             boolean expired,
                             String userEmail,
                             UUID newTokenId,
                             String newRawToken,
                             LocalDateTime newExpiresAt
        ) {
            this.success = success;
            this.invalid = invalid;
            this.reused = reused;
            this.expired = expired;
            this.userEmail = userEmail;
            this.newTokenId = newTokenId;
            this.newRawToken = newRawToken;
            this.newExpiresAt = newExpiresAt;
        }

        public static RotateResult rotated(String userEmail, UUID id, String raw, LocalDateTime exp) {
            return new RotateResult(true, false, false, false, userEmail, id, raw, exp);
        }
        public static RotateResult invalid() {
            return new RotateResult(false, true, false, false, null, null, null, null);
        }
        public static RotateResult reused() {
            return new RotateResult(false, false, true, false, null, null, null, null);
        }
        public static RotateResult expired() {
            return new RotateResult(false, false, false, true, null, null, null, null);
        }
    }

    public record ActiveToken(UUID tokenId, String userEmail, LocalDateTime expiresAt) {}
}