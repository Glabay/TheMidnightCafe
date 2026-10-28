package dev.midnightcoder.cafe.security.oauth;

import dev.midnightcoder.cafe.security.CookieHelper;
import dev.midnightcoder.cafe.security.authtentication.IdentificationDetailsService;
import dev.midnightcoder.cafe.security.authtentication.jwt.JwtService;
import dev.midnightcoder.cafe.security.authtentication.jwt.refresh.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-30
 */
@Component
@NullMarked
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieHelper cookieHelper;
    private final IdentificationDetailsService identificationDetailsService;

    @Value("${jwt.access-ttl-minutes:15}")
    private int accessTtlMinutes;

    @Value("${jwt.refresh-ttl-days:14}")
    private int refreshTtlDays;

    @Value("${app.oauth2.redirect-uri:/?loginSuccess=true}")
    private String redirectUri;

    public OAuth2AuthenticationSuccessHandler(
        JwtService jwtService,
        RefreshTokenService refreshTokenService,
        CookieHelper cookieHelper,
        IdentificationDetailsService identificationDetailsService
    ) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.cookieHelper = cookieHelper;
        this.identificationDetailsService = identificationDetailsService;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        String identity;
        if (authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
            identity = customOAuth2User.getIdentity();
        } else {
            identity = authentication.getName();
        }

        log.debug("OAuth2 authentication successful for user: {}", identity);

        var userDetails = identificationDetailsService.loadUserByUsername(identity);
        var roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        var accessToken = jwtService.generateAccessToken(identity, roles);

        var userAgent = request.getHeader("User-Agent");
        var ipAddress = request.getRemoteAddr();
        var refreshToken = refreshTokenService.issue(identity, userAgent, ipAddress);

        cookieHelper.setAccessCookie(response, accessToken, Duration.ofMinutes(accessTtlMinutes));
        cookieHelper.setRefreshCookie(response, refreshToken.rawToken(), Duration.ofDays(refreshTtlDays));

        clearAuthenticationAttributes(request);

        response.sendRedirect(redirectUri);
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}
