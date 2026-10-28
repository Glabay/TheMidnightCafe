package dev.midnightcoder.cafe.security.oauth;

import dev.midnightcoder.cafe.users.api.UserDto;
import dev.midnightcoder.cafe.users.api.UserLookup;
import dev.midnightcoder.cafe.users.api.UserRegistration;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-30
 */
@Service
@NullMarked
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserLookup userLookup;
    private final UserRegistration userRegistration;

    public CustomOAuth2UserService(UserLookup userLookup, UserRegistration userRegistration) {
        this.userLookup = userLookup;
        this.userRegistration = userRegistration;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }

    public OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.debug("Processing OAuth2 login for provider: {}", registrationId);

        OAuth2UserInfo userInfo = extractUserInfo(registrationId, oAuth2User);
        String identity = userInfo.getPreferredIdentity();

        if (identity == null || identity.isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_identity", "Identity not found from " + registrationId + " provider", null)
            );
        }

        UserDto userDto = userLookup.findByIdentity(identity)
            .orElseGet(() -> {
                log.info("Registering new OAuth user with identity '{}' via provider '{}'", identity, registrationId);
                return userRegistration.registerOAuthUser(identity);
            });

        Set<GrantedAuthority> authorities = new HashSet<>();
        if (userDto.roles() != null) {
            for (var role : userDto.roles()) {
                String roleName = role.getRole().name();
                String roleWithPrefix = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
            }
        }

        String userNameAttributeName = userRequest.getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName();

        if (userNameAttributeName == null || userNameAttributeName.isBlank()) {
            userNameAttributeName = "id";
        }

        return new CustomOAuth2User(userDto, oAuth2User.getAttributes(), authorities, userNameAttributeName);
    }

    private OAuth2UserInfo extractUserInfo(String registrationId, OAuth2User oAuth2User) {
        if ("github".equalsIgnoreCase(registrationId)) {
            return new GitHubOAuth2UserInfo(oAuth2User.getAttributes());
        }
        // Default to GitHub user info format or generic fallback
        return new GitHubOAuth2UserInfo(oAuth2User.getAttributes());
    }
}
