package dev.midnightcoder.cafe.security.autherization;

import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-24
 */
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private MidnightRoles role;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<MidnightPermissions> permissions;

    public Role(MidnightRoles role) {
        this.role = role;
    }

    public Role() {
    }

    public MidnightRoles getRole() {
        return role;
    }

    public Set<MidnightPermissions> getPermissions() {
        return permissions;
    }
}
