package dev.midnightcoder.cafe.web;

import dev.midnightcoder.cafe.products.ProductService;
import dev.midnightcoder.cafe.products.api.Category;
import dev.midnightcoder.cafe.products.api.ProductCreationRequest;
import dev.midnightcoder.cafe.products.api.ProductDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-20
 */
@Controller
@PreAuthorize("hasRole('ADMIN') and hasAuthority('permission:product:read')")
@RequestMapping("/admin/products")
public class AdminProductDashboard {
    private final ProductService productService;

    public AdminProductDashboard(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('permission:product:create')")
    public String createProduct(ProductCreationRequest form) {
        productService.registerNewProduct(form);
        return "redirect:/admin/products";
    }

    @GetMapping
    public String products(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("productCount", productService.count());
        return "admin/product";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('permission:product:create')")
    public String newProduct(Model model) {
        model.addAttribute("product", new ProductDto(
            null,
            "",
            "",
            BigDecimal.ZERO,
            Category.BEVERAGE,
            null,
            null
        ));
        model.addAttribute("categories", Category.values());
        return "admin/product-form";
    }

    @PostMapping("/{productId}")
    @PreAuthorize("hasAuthority('permission:product:edit')")
    public String updateProduct(@PathVariable UUID productId, ProductCreationRequest form) {
        productService.update(productId, form);
        return "redirect:/admin/products";
    }

    @GetMapping("/{productId}")
    public String viewProduct(@PathVariable UUID productId, Model model) {
        var product = productService.findById(productId);
        if (product.isEmpty()) {
            return "redirect:/admin/products";
        }
        model.addAttribute("product", product.get());
        return "admin/product-view";
    }

    @GetMapping("/{productId}/edit")
    @PreAuthorize("hasAuthority('permission:product:edit')")
    public String editProduct(@PathVariable UUID productId, Model model) {
        var product = productService.findById(productId);
        if (product.isEmpty()) {
            return "redirect:/admin/products";
        }
        model.addAttribute("product", product.get());
        model.addAttribute("categories", Category.values());
        return "admin/product-form";
    }
}
