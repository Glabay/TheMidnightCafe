package dev.midnightcoder.cafe.web;

import dev.midnightcoder.cafe.customers.CustomerService;
import dev.midnightcoder.cafe.customers.api.CustomerCreationRequest;
import dev.midnightcoder.cafe.customers.api.CustomerDto;
import dev.midnightcoder.cafe.users.api.UserLookup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
@RequestMapping("/admin/customers")
public class AdminCustomerDashboard {
    private final CustomerService customerService;
    private final UserLookup userLookup;

    public AdminCustomerDashboard(CustomerService customerService, UserLookup userLookup) {
        this.customerService = customerService;
        this.userLookup = userLookup;
    }

    @PostMapping
    public String createCustomer(CustomerCreationRequest form) {
        customerService.registerNewCustomer(form);
        return "redirect:/admin/customers";
    }

    @GetMapping
    public String customers(Model model) {
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("customerCount", customerService.count());
        return "admin/customer";
    }

    @GetMapping("/new")
    public String newCustomer(Model model) {
        model.addAttribute("customer", new CustomerDto(
            null,
            null,
            "",
            "",
            "",
            "",
            "",
            null,
            0,
            false,
            null,
            null
        ));
        model.addAttribute("users", userLookup.findAll());
        return "admin/customer-form";
    }

    @PostMapping("/{customerId}")
    public String updateCustomer(@PathVariable UUID customerId, CustomerCreationRequest form) {
        customerService.update(customerId, form);
        return "redirect:/admin/customers";
    }

    @GetMapping("/{customerId}")
    public String viewCustomer(@PathVariable UUID customerId, Model model) {
        var customerOpt = customerService.findById(customerId);
        if (customerOpt.isEmpty()) {
            return "redirect:/admin/customers";
        }
        model.addAttribute("customer", customerOpt.get());
        if (customerOpt.get().userId() != null) {
            userLookup.findByUserId(customerOpt.get().userId())
                .ifPresent(user -> model.addAttribute("linkedUser", user));
        }
        return "admin/customer-view";
    }

    @GetMapping("/{customerId}/edit")
    public String editCustomer(@PathVariable UUID customerId, Model model) {
        var customerOpt = customerService.findById(customerId);
        if (customerOpt.isEmpty()) {
            return "redirect:/admin/customers";
        }
        model.addAttribute("customer", customerOpt.get());
        model.addAttribute("users", userLookup.findAll());
        return "admin/customer-form";
    }

}
