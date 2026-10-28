package dev.midnightcoder.cafe.employees.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-14
 */
public record EmployeeDto(
    UUID id,
    UUID userId,
    UUID customerId,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    String jobTitle,
    String role,
    EmployeeStatus status,
    LocalDate hireDate,
    BigDecimal hourlyRate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public EmployeeDto(
        UUID userId,
        UUID customerId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String jobTitle,
        EmployeeStatus status,
        LocalDate hireDate,
        BigDecimal hourlyRate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this(null, userId, customerId, firstName, lastName, email, phoneNumber, jobTitle, "ROLE_EMPLOYEE", status, hireDate, hourlyRate, createdAt, updatedAt);
    }
}
