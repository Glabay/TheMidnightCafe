package dev.midnightcoder.cafe.security.autherization;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-24
 */
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private MidnightPermissions midnightPermissions;

    public Permission(MidnightPermissions midnightPermissions) {
        this.midnightPermissions = midnightPermissions;
    }

    public Permission() {}

    public MidnightPermissions getMidnightPermissions() {
        return midnightPermissions;
    }

    public void setMidnightPermissions(MidnightPermissions midnightPermissions) {
        this.midnightPermissions = midnightPermissions;
    }
}