package dev.midnightcoder.cafe.security.authtentication;

import dev.midnightcoder.cafe.users.api.UserDto;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-14
 */
@NullMarked
public class IdentificationDetails implements UserDetails {
    private final String username;
    private final String password;
    private final boolean enabled;
    private final UserDto userDto;

    public IdentificationDetails(UserDto userDto) {
        this.userDto = userDto;
        this.username = userDto.identity();
        this.password = userDto.passwordHash();
        this.enabled = userDto.enabled();
    }

    public UserDto getUserDto() {
        return userDto;
    }

    public UserDto userDto() {
        return userDto;
    }

    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        var authorities = new ArrayList<SimpleGrantedAuthority>();
        userDto.roles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getRole().name()));
            role.getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getAuthority())));
        });
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
