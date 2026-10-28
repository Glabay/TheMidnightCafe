package dev.midnightcoder.cafe.web;

import dev.midnightcoder.cafe.customers.CustomerService;
import dev.midnightcoder.cafe.employees.EmployeeService;
import dev.midnightcoder.cafe.products.ProductService;
import dev.midnightcoder.cafe.users.api.UserLookup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-20
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboard {
    private final ProductService productService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final UserLookup userLookup;

    public AdminDashboard(ProductService productService,
                          CustomerService customerService,
                          EmployeeService employeeService,
                          UserLookup userLookup
    ) {
        this.productService = productService;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.userLookup = userLookup;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        model.addAttribute("productCount", productService.count());
        model.addAttribute("customerCount", customerService.count());
        model.addAttribute("employeeCount", employeeService.count());
        model.addAttribute("userCount", userLookup.findAll().size());

        model.addAttribute("products", productService.findAll());
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("employees", employeeService.findAll());
        return "admin/dashboard";
    }
}
