package dev.midnightcoder.cafe.security.oauth;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-30
 */
@NullMarked
public record GitHubOAuth2UserInfo(
    Map<String, Object> attributes
) implements OAuth2UserInfo {

    public GitHubOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    @Override
    @Nullable
    public String getId() {
        var id = attributes.get("id");
        return id != null ? String.valueOf(id) : null;
    }

    @Nullable
    public String getLogin() {
        var login = attributes.get("login");
        return login instanceof String str ? str : null;
    }

    @Override
    @Nullable
    public String getName() {
        var name = attributes.get("name");
        return name instanceof String str ? str : null;
    }

    @Override
    @Nullable
    public String getEmail() {
        var email = attributes.get("email");
        return email instanceof String str ? str : null;
    }

    @Override
    @Nullable
    public String getImageUrl() {
        var avatarUrl = attributes.get("avatar_url");
        return avatarUrl instanceof String str ? str : null;
    }

    @Override
    @Nullable
    public String getPreferredIdentity() {
        var name = getName();
        if (name != null && !name.isBlank())
            return name;
        var login = getLogin();
        if (login != null && !login.isBlank())
            return login;
        var email = getEmail();
        if (email != null && !email.isBlank())
            return email;
        return getId();
    }
}
