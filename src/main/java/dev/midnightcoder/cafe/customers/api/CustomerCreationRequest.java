package dev.midnightcoder.cafe.customers.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
public record CustomerCreationRequest(
    UUID userId,
    @NotBlank(message = "First name cannot be empty")
    String firstName,
    @NotBlank(message = "Last name cannot be empty")
    String lastName,
    @Email(message = "Email must be valid")
    String email,
    @NotBlank(message = "Phone number cannot be empty")
    String phoneNumber,
    String preferredName,
    LocalDate birthday,
    Integer loyaltyPoints,
    Boolean marketingOptIn
) {
    public CustomerCreationRequest(UUID userId, String firstName, String lastName, String email, String phoneNumber) {
        this(userId, firstName, lastName, email, phoneNumber, null, null, 0, false);
    }
}
