package dev.midnightcoder.cafe.security.authtentication.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author Glabay | Glabay-Studios
 * @project Midnight-Coder
 * @social Discord: Glabay
 * @since 2026-01-03
 */
@Service
@NullMarked
public class JwtService {
    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    @Value("${jwt.access-ttl-minutes}")
    private int accessTtlMinutes;

    @Value("${jwt.clock-skew-seconds}")
    private long clockSkewSeconds;

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        if (secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured. Set jwt.secret or JWT_SECRET env var.");
        }
        byte[] keyBytes = deriveStrongKey(secret.trim());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] deriveStrongKey(String rawSecret) {
        byte[] candidate;
        try {
            candidate = Decoders.BASE64.decode(rawSecret);
        }
        catch (IllegalArgumentException ignored) {
            candidate = rawSecret.getBytes(StandardCharsets.UTF_8);
        }

        if (candidate.length < 32) {
            try {
                var digest = MessageDigest.getInstance("SHA-256");
                candidate = digest.digest(candidate);
            }
            catch (Exception e) {
                throw new IllegalStateException("Failed to derive strong JWT signing key", e);
            }
        }
        return candidate;
    }

    public String generateAccessToken(String subject, Collection<@Nullable String> roles) {
        var now = Instant.now();
        var exp = now.plus(accessTtlMinutes, ChronoUnit.MINUTES);

        var claims = new HashMap<String, Object>();
            claims.put("roles", new ArrayList<>(roles));

        return Jwts.builder().issuer(issuer)
            .audience().add(audience)
            .and()
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claims(claims)
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            return Jwts.parser()
                .requireIssuer(issuer)
                .requireAudience(audience)
                .clockSkewSeconds(clockSkewSeconds)
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token) != null;
        }
        catch (Exception e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
            .requireIssuer(issuer)
            .requireAudience(audience)
            .clockSkewSeconds(clockSkewSeconds)
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public List<String> extractRoles(Claims claims) {
        var raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            var roles = new ArrayList<String>();
            for (var o : list)
                roles.add(o.toString());
            return roles;
        }
        if (raw instanceof String s)
            return Arrays.asList(s.split(","));
        return Collections.emptyList();
    }
}
