package dev.midnightcoder.cafe.users;

import dev.midnightcoder.cafe.security.autherization.Role;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-11
 */
@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(unique = true, nullable = false)
    private String identity;

    @Column(nullable = false)
    private String passwordHash;

    private Boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    protected User() {
    }

    User(String identity, String passwordHash, Boolean enabled) {
        this.identity = identity;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.roles = new HashSet<>();
    }

    public UUID getId() {
        return id;
    }

    public String getIdentity() {
        return identity;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
