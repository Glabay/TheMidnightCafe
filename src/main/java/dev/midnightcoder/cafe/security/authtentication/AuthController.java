package dev.midnightcoder.cafe.security.authtentication;

import dev.midnightcoder.cafe.security.CookieHelper;
import dev.midnightcoder.cafe.security.authtentication.jwt.JwtService;
import dev.midnightcoder.cafe.security.authtentication.jwt.refresh.RefreshTokenService;
import dev.midnightcoder.cafe.users.api.RegistrationRequest;
import dev.midnightcoder.cafe.users.api.UserAlreadyExistsException;
import dev.midnightcoder.cafe.users.api.UserDto;
import dev.midnightcoder.cafe.users.api.UserRegistration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-14
 */
@NullMarked
@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final UserRegistration userRegistration;
    private final IdentificationDetailsService identificationDetailsService;
    private final CookieHelper cookieHelper;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.access-ttl-minutes}")
    private int accessTtlMinutes;
    @Value("${jwt.refresh-ttl-days}")
    private int refreshTtlDays;

    AuthController(
        UserRegistration userRegistration,
        IdentificationDetailsService identificationDetailsService,
        CookieHelper cookieHelper,
        JwtService jwtService,
        RefreshTokenService refreshTokenService
    ) {
        this.userRegistration = userRegistration;
        this.identificationDetailsService = identificationDetailsService;
        this.cookieHelper = cookieHelper;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("register")
    public ResponseEntity<UserDto> register(@ModelAttribute RegistrationRequest request) {
        UserDto registeredUser;
        try {
            registeredUser = userRegistration.register(request);
        }
        catch (UserAlreadyExistsException e) {
            return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .location(URI.create("/register?error=identity"))
                .build();
        }
        catch (PasswordMissMatchException e) {
            return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .location(URI.create("/register?error=password"))
                .build();
        }
        return ResponseEntity
            .status(HttpStatus.SEE_OTHER)
            .location(URI.create("/login"))
            .body(registeredUser);
    }

    @PostMapping("login")
    public ResponseEntity<UserDto> login(HttpServletRequest request,
                                         HttpServletResponse response,
                                         @ModelAttribute LoginRequest loginRequest
    ) {
        var user = identificationDetailsService.loadUserByUsername(loginRequest.identity());
        var ip = request.getRemoteAddr();
        var roles = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

        var jwt = jwtService.generateAccessToken(user.getUsername(), roles);
        var refreshToken = refreshTokenService.issue(user.getUsername(), request.getHeader("User-Agent"), ip);

        cookieHelper.setAccessCookie(response, jwt, Duration.ofMinutes(accessTtlMinutes));
        cookieHelper.setRefreshCookie(response, refreshToken.rawToken(), Duration.ofDays(refreshTtlDays));

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
            .location(URI.create("/"))
            .body(user.getUserDto());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleException(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleException(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Invalid identity or password"));
    }
}
