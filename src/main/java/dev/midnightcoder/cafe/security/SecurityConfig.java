package dev.midnightcoder.cafe.security;

import dev.midnightcoder.cafe.security.authtentication.jwt.JwtAuthFilter;
import dev.midnightcoder.cafe.security.authtentication.jwt.refresh.RefreshTokenService;
import dev.midnightcoder.cafe.security.oauth.CustomOAuth2UserService;
import dev.midnightcoder.cafe.security.oauth.OAuth2AuthenticationFailureHandler;
import dev.midnightcoder.cafe.security.oauth.OAuth2AuthenticationSuccessHandler;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-14
 */
@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {
    private final CookieHelper cookieHelper;
    private final JwtAuthFilter jwtAuthFilter;
    private final RefreshTokenService refreshTokenService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oauth2FailureHandler;

    public SecurityConfig(
        CookieHelper cookieHelper,
        JwtAuthFilter jwtAuthFilter,
        RefreshTokenService refreshTokenService,
        CustomOAuth2UserService customOAuth2UserService,
        OAuth2AuthenticationSuccessHandler oauth2SuccessHandler,
        OAuth2AuthenticationFailureHandler oauth2FailureHandler
    ) {
        this.cookieHelper = cookieHelper;
        this.jwtAuthFilter = jwtAuthFilter;
        this.refreshTokenService = refreshTokenService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.oauth2FailureHandler = oauth2FailureHandler;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
            "/css/**",
            "/js/**",
            "/img/**"
        ).addResourceLocations(
            "classpath:/static/css/",
            "classpath:/static/js/",
            "classpath:/static/img/"
        ).setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/api/**",
                    "/register",
                    "/login",
                    "/auth/**",
                    "/oauth2/**",
                    "/login/oauth2/**"
                )
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.GET,
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/error"
                ).permitAll()
                .requestMatchers(
                    "/",
                    "/register",
                    "/login",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/admin/**",
                    "/api/v1/**",
                    "/products/**",
                    "/api/auth/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/?loginSuccess=true", true)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oauth2SuccessHandler)
                .failureHandler(oauth2FailureHandler)
            )
            .httpBasic(Customizer.withDefaults())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler((request, response, _) -> {
                    cookieHelper.getCookie(request, CookieHelper.REFRESH_COOKIE)
                        .ifPresent(refreshTokenService::revokeCurrent);
                    cookieHelper.clearAuthCookies(response);
                })
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );

        return http.build();
    }
}
