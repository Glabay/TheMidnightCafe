package dev.midnightcoder.cafe.employees.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
public record EmployeeCreationRequest(
    UUID userId,
    UUID customerId,
    @NotBlank(message = "First name cannot be empty")
    String firstName,
    @NotBlank(message = "Last name cannot be empty")
    String lastName,
    @Email(message = "Email must be valid")
    String email,
    @NotBlank(message = "Phone number cannot be empty")
    String phoneNumber,
    String jobTitle,
    String role,
    EmployeeStatus status,
    LocalDate hireDate,
    @NotNull(message = "Hourly rate cannot be null")
    BigDecimal hourlyRate
) {
    public EmployeeCreationRequest(
        UUID userId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        BigDecimal hourlyRate
    ) {
        this(userId, null, firstName, lastName, email, phoneNumber, "Barista", "ROLE_EMPLOYEE", EmployeeStatus.ACTIVE, LocalDate.now(), hourlyRate);
    }
}
