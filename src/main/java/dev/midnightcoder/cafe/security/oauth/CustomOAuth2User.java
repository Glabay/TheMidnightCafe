package dev.midnightcoder.cafe.security.oauth;

import dev.midnightcoder.cafe.users.api.UserDto;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
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
public class CustomOAuth2User implements OAuth2User, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final UserDto userDto;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String nameAttributeKey;

    public CustomOAuth2User(
        UserDto userDto,
        Map<String, Object> attributes,
        Collection<? extends GrantedAuthority> authorities,
        String nameAttributeKey
    ) {
        this.userDto = userDto;
        this.attributes = Collections.unmodifiableMap(attributes);
        this.authorities = Collections.unmodifiableCollection(authorities);
        this.nameAttributeKey = nameAttributeKey;
    }

    public UserDto getUserDto() {
        return userDto;
    }

    public String getIdentity() {
        return userDto.identity();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        Object nameVal = attributes.get(nameAttributeKey);
        if (nameVal != null)
            return String.valueOf(nameVal);
        return userDto.identity();
    }
}
