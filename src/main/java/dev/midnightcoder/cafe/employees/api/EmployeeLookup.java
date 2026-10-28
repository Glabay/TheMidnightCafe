package dev.midnightcoder.cafe.employees.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-14
 */
public interface EmployeeLookup {
    Optional<EmployeeDto> findByUserId(UUID userId);
    Optional<EmployeeDto> findById(UUID id);
    Optional<EmployeeDto> findByEmail(String email);

    List<EmployeeDto> findAllByStatusIs(EmployeeStatus status);
    List<EmployeeDto> findAll();
    Long count();
}
