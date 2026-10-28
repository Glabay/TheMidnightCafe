package dev.midnightcoder.cafe.security.authtentication.jwt;

import dev.midnightcoder.cafe.security.CookieHelper;
import dev.midnightcoder.cafe.security.authtentication.IdentificationDetailsService;
import dev.midnightcoder.cafe.security.authtentication.jwt.refresh.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;

/**
 * @author Glabay | Glabay-Studios
 * @project Midnight-Coder
 * @social Discord: Glabay
 * @since 2026-01-03
 */
@Component
@NullMarked
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String ACCESS_COOKIE = CookieHelper.ACCESS_COOKIE;

    private final IdentificationDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieHelper cookieHelper;

    @Value("${jwt.access-ttl-minutes}")
    private int accessTtlMinutes;

    public JwtAuthFilter(
        IdentificationDetailsService userDetailsService,
        JwtService jwtService,
        RefreshTokenService refreshTokenService,
        CookieHelper cookieHelper
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.cookieHelper = cookieHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            var token = extractCookie(request, ACCESS_COOKIE);
            var refreshToken = extractCookie(request, CookieHelper.REFRESH_COOKIE);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                if (!token.isBlank()) {
                    if (jwtService.isTokenValid(token)) {
                        authenticate(token, request);
                    } else if (!refreshToken.isBlank()) {
                        // Token is invalid/expired, try to refresh
                        log.debug("Access token expired/invalid, attempting refresh");
                        var newToken = attemptRefresh(request, response, refreshToken);
                        if (newToken != null) {
                            authenticate(newToken, request);
                        }
                    }
                } else if (!refreshToken.isBlank()) {
                    // No access token but has refresh token, try to refresh
                    log.debug("Access token missing, attempting refresh with refresh token");
                    var newToken = attemptRefresh(request, response, refreshToken);
                    if (newToken != null) {
                        authenticate(newToken, request);
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("Authentication filter error", e);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        var claims = jwtService.parseClaims(token);
        var principal = claims.getSubject();
        var roles = jwtService.extractRoles(claims);
        var authorities = new ArrayList<GrantedAuthority>();
        for (var r : roles) {
            if (!r.isBlank()) {
                String roleWithPrefix = r.startsWith("ROLE_") ? r : "ROLE_" + r;
                authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
            }
        }
        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @org.jspecify.annotations.Nullable
    private String attemptRefresh(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        var result = refreshTokenService.rotate(refreshToken, request.getHeader("User-Agent"), request.getRemoteAddr());
        if (result.success) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(result.userEmail);
            var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

            var newAccessToken = jwtService.generateAccessToken(userDetails.getUsername(), roles);

            // Set cookies
            cookieHelper.setAccessCookie(response, newAccessToken, Duration.ofMinutes(accessTtlMinutes));
            // Rotate refresh token cookie too (since it was rotated in the service)
            // Need refreshTtlDays, but we can use the result.newExpiresAt if we want,
            // or just use a fixed value. Let's use 14 days as in AuthController or get it from properties.
            // For simplicity, let's just use the Duration between now and newExpiresAt.
            var ttl = Duration.between(java.time.LocalDateTime.now(), result.newExpiresAt);
            cookieHelper.setRefreshCookie(response, result.newRawToken, ttl);

            return newAccessToken;
        }
        return null;
    }

    private String extractCookie(HttpServletRequest request, String name) {
        var cookies = request.getCookies();
        if (cookies == null) return "";
        for (var c : cookies) {
            if (name.equals(c.getName()))
                return c.getValue();
        }
        return "";
    }
}
