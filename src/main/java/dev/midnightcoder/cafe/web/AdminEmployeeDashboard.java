package dev.midnightcoder.cafe.web;

import dev.midnightcoder.cafe.employees.EmployeeService;
import dev.midnightcoder.cafe.employees.api.EmployeeCreationRequest;
import dev.midnightcoder.cafe.employees.api.EmployeeDto;
import dev.midnightcoder.cafe.employees.api.EmployeeStatus;
import dev.midnightcoder.cafe.security.autherization.MidnightRoles;
import dev.midnightcoder.cafe.users.api.UserLookup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-20
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/employees")
public class AdminEmployeeDashboard {
    private final EmployeeService employeeService;
    private final UserLookup userLookup;

    public AdminEmployeeDashboard(EmployeeService employeeService, UserLookup userLookup) {
        this.employeeService = employeeService;
        this.userLookup = userLookup;
    }

    @PostMapping
    public String createEmployee(EmployeeCreationRequest form) {
        employeeService.registerNewEmployee(form);
        return "redirect:/admin/employees";
    }

    @GetMapping
    public String employees(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("employeeCount", employeeService.count());
        return "admin/employee";
    }

    @GetMapping("/new")
    public String newEmployee(@RequestParam(value = "userId", required = false) UUID userId, Model model) {
        String email = "";
        String firstName = "";
        if (userId != null) {
            var userOpt = userLookup.findByUserId(userId);
            if (userOpt.isPresent()) {
                var identity = userOpt.get().identity();
                if (identity.contains("@"))
                    email = identity;
                else
                    firstName = identity;
            }
        }

        model.addAttribute("employee", new EmployeeDto(
            null,
            userId,
            null,
            firstName,
            "",
            email,
            "",
            "Barista",
            "ROLE_EMPLOYEE",
            EmployeeStatus.ACTIVE,
            LocalDate.now(),
            BigDecimal.valueOf(17.50),
            null,
            null
        ));
        model.addAttribute("users", userLookup.findAll());
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("statuses", EmployeeStatus.values());
        model.addAttribute("roles", MidnightRoles.values());
        model.addAttribute("jobTitles", getDefaultTitles());
        return "admin/employee-form";
    }

    @PostMapping("/{employeeId}")
    public String updateEmployee(@PathVariable UUID employeeId, EmployeeCreationRequest form) {
        employeeService.update(employeeId, form);
        return "redirect:/admin/employees";
    }

    @GetMapping("/{employeeId}")
    public String viewEmployee(@PathVariable UUID employeeId, Model model) {
        var employeeOpt = employeeService.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/admin/employees";
        }
        model.addAttribute("employee", employeeOpt.get());
        if (employeeOpt.get().userId() != null) {
            userLookup.findByUserId(employeeOpt.get().userId())
                .ifPresent(user -> model.addAttribute("linkedUser", user));
        }
        return "admin/employee-view";
    }

    @GetMapping("/{employeeId}/edit")
    public String editEmployee(@PathVariable UUID employeeId, Model model) {
        var employeeOpt = employeeService.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/admin/employees";
        }
        model.addAttribute("employee", employeeOpt.get());
        model.addAttribute("users", userLookup.findAll());
        model.addAttribute("statuses", EmployeeStatus.values());
        model.addAttribute("roles", MidnightRoles.values());
        model.addAttribute("jobTitles", getDefaultTitles());
        return "admin/employee-form";
    }

    private List<String> getDefaultTitles() {
        return Arrays.stream(MidnightRoles.values())
            .map(MidnightRoles::getRoleTitle)
            .toList();
    }
}
