package dev.midnightcoder.cafe.security.authtentication;

import dev.midnightcoder.cafe.users.api.UserLookup;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-14
 */
@Service
@NullMarked
public class IdentificationDetailsService implements UserDetailsService {
    private final UserLookup userLookup;

    public IdentificationDetailsService(UserLookup userLookup) {
        this.userLookup = userLookup;
    }

    @Override
    public IdentificationDetails loadUserByUsername(@Nullable String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank())
            throw new UsernameNotFoundException("Username cannot be empty");

        var user = userLookup.findByIdentity(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with identity: " + username));

        return new IdentificationDetails(user);
    }
}
