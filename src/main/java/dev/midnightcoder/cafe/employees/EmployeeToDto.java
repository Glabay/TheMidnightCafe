package dev.midnightcoder.cafe.employees;

import dev.midnightcoder.cafe.employees.api.EmployeeDto;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
interface EmployeeToDto {

    default EmployeeDto convert(Employee cachedEmployee) {
        return new EmployeeDto(
            cachedEmployee.getId(),
            cachedEmployee.getUserId(),
            cachedEmployee.getCustomerId(),
            cachedEmployee.getFirstName(),
            cachedEmployee.getLastName(),
            cachedEmployee.getEmail(),
            cachedEmployee.getPhoneNumber(),
            cachedEmployee.getJobTitle(),
            cachedEmployee.getRole() != null ? cachedEmployee.getRole() : "ROLE_EMPLOYEE",
            cachedEmployee.getStatus(),
            cachedEmployee.getHireDate(),
            cachedEmployee.getHourlyRate(),
            cachedEmployee.getCreatedAt(),
            cachedEmployee.getUpdatedAt()
        );
    }
}
