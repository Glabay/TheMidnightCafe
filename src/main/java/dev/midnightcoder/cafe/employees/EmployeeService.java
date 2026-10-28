package dev.midnightcoder.cafe.employees;

import dev.midnightcoder.cafe.employees.api.EmployeeCreationRequest;
import dev.midnightcoder.cafe.employees.api.EmployeeDto;
import dev.midnightcoder.cafe.employees.api.EmployeeLookup;
import dev.midnightcoder.cafe.employees.api.EmployeeStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
@Service
public class EmployeeService implements EmployeeLookup, EmployeeToDto {
    private final EmployeeRepository repository;

    EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<EmployeeDto> findByUserId(UUID userId) {
        return repository.findByUserId(userId).map(this::convert);
    }

    @Override
    public Optional<EmployeeDto> findById(UUID id) {
        return repository.findById(id).map(this::convert);
    }

    @Override
    public Optional<EmployeeDto> findByEmail(String email) {
        return repository.findByEmail(email).map(this::convert);
    }

    @Override
    public List<EmployeeDto> findAllByStatusIs(EmployeeStatus status) {
        return repository.findAllByStatusIs(status).stream().map(this::convert).toList();
    }

    @Override
    public List<EmployeeDto> findAll() {
        return repository.findAll().stream().map(this::convert).toList();
    }

    public EmployeeDto registerNewEmployee(EmployeeCreationRequest request) {
        var employee = new Employee();
        employee.setUserId(request.userId());
        employee.setCustomerId(request.customerId());
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhoneNumber(request.phoneNumber());
        employee.setJobTitle(request.jobTitle() != null && !request.jobTitle().isBlank() ? request.jobTitle() : "Barista");
        employee.setRole(request.role() != null && !request.role().isBlank() ? request.role() : "ROLE_EMPLOYEE");
        employee.setStatus(request.status() != null ? request.status() : EmployeeStatus.ACTIVE);
        employee.setHireDate(request.hireDate() != null ? request.hireDate() : LocalDate.now());
        employee.setHourlyRate(request.hourlyRate());

        return convert(repository.save(employee));
    }

    public Optional<EmployeeDto> update(UUID id, EmployeeCreationRequest form) {
        return repository.findById(id).map(employee -> {
            if (form.userId() != null) employee.setUserId(form.userId());
            if (form.customerId() != null) employee.setCustomerId(form.customerId());
            employee.setFirstName(form.firstName());
            employee.setLastName(form.lastName());
            employee.setEmail(form.email());
            employee.setPhoneNumber(form.phoneNumber());
            if (form.jobTitle() != null) employee.setJobTitle(form.jobTitle());
            if (form.role() != null) employee.setRole(form.role());
            if (form.status() != null) employee.setStatus(form.status());
            if (form.hireDate() != null) employee.setHireDate(form.hireDate());
            if (form.hourlyRate() != null) employee.setHourlyRate(form.hourlyRate());
            return convert(repository.save(employee));
        });
    }

    public Optional<EmployeeDto> update(UUID id, EmployeeDto form) {
        return repository.findById(id).map(employee -> {
            if (form.userId() != null) employee.setUserId(form.userId());
            if (form.customerId() != null) employee.setCustomerId(form.customerId());
            employee.setFirstName(form.firstName());
            employee.setLastName(form.lastName());
            employee.setEmail(form.email());
            employee.setPhoneNumber(form.phoneNumber());
            if (form.jobTitle() != null) employee.setJobTitle(form.jobTitle());
            if (form.role() != null) employee.setRole(form.role());
            if (form.status() != null) employee.setStatus(form.status());
            if (form.hireDate() != null) employee.setHireDate(form.hireDate());
            if (form.hourlyRate() != null) employee.setHourlyRate(form.hourlyRate());
            return convert(repository.save(employee));
        });
    }

    @Override
    public Long count() {
        return repository.countAllBy();
    }
}
