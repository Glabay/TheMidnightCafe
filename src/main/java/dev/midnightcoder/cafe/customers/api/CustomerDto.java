package dev.midnightcoder.cafe.customers.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
public record CustomerDto(
    UUID id,
    UUID userId,
    UUID cartId,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    String preferredName,
    LocalDate birthday,
    Integer loyaltyPoints,
    Boolean marketingOptIn,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public CustomerDto(UUID userId, UUID cartId, String firstName, String lastName, String email, String phoneNumber, String preferredName, LocalDate birthday, Integer loyaltyPoints, Boolean marketingOptIn, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(null, userId, cartId, firstName, lastName, email, phoneNumber, preferredName, birthday, loyaltyPoints, marketingOptIn, createdAt, updatedAt);
    }
}