package dev.midnightcoder.cafe.web;

import dev.midnightcoder.cafe.products.Product;
import dev.midnightcoder.cafe.products.ProductService;
import dev.midnightcoder.cafe.products.api.Category;
import dev.midnightcoder.cafe.products.api.ProductDto;
import dev.midnightcoder.cafe.products.api.ProductToDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
@Controller
@RequestMapping("/")
public class SiteMap implements ProductToDto {
    private final ProductService productService;

    public SiteMap(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String getSiteMap(Model model) {
        var featuredProducts = productService.findAll().stream()
            .limit(3)
            .map(this::convert)
            .toList();
        model.addAttribute("featuredProducts", featuredProducts);
        return "index";
    }

    @GetMapping("/login")
    public String getLogin() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String getRegister() {
        return "auth/register";
    }

    @GetMapping("/products")
    public String getProducts(
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "20") int size,
        @RequestParam(name = "category", required = false) String categoryStr,
        @RequestParam(name = "sort", defaultValue = "name,asc") String sortParam,
        Model model
    ) {
        int pageIndex = Math.max(0, page - 1);
        int pageSize = size > 0 ? size : 20;

        var sort = parseSort(sortParam);
        var pageable = PageRequest.of(pageIndex, pageSize, sort);

        Optional<Category> categoryOpt = Category.fromString(categoryStr);
        Page<Product> productPage;
        if (categoryOpt.isPresent())
            productPage = categoryOpt.map(category -> productService.findByCategory(category, pageable))
                .orElseGet(() -> productService.findAll(pageable));
        else
            productPage = productService.findAll(pageable);

        List<ProductDto> products = productPage.getContent().stream()
            .map(this::convert)
            .toList();

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("hasNext", productPage.hasNext());
        model.addAttribute("hasPrevious", productPage.hasPrevious());
        model.addAttribute("selectedCategory", categoryOpt.map(Enum::name).orElse("ALL"));
        model.addAttribute("selectedSort", sortParam);
        model.addAttribute("categories", Category.values());

        return "products";
    }

    @GetMapping("/products/{id}")
    public String getProducts(@PathVariable("id") UUID id, Model model) {
        var product = productService.findById(id);
        if (product.isEmpty())
            return "redirect:/products";
        var dto = convert(product.get());
        model.addAttribute("product", dto);
        return "product-view";
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "name");
        }
        var lower = sortParam.trim().toLowerCase();
        return switch (lower) {
            case "name,desc", "name_desc" -> Sort.by(Sort.Direction.DESC, "name");
            case "price,asc", "price_asc", "price_low" -> Sort.by(Sort.Direction.ASC, "price");
            case "price,desc", "price_desc", "price_high" -> Sort.by(Sort.Direction.DESC, "price");
            case "category,asc", "category", "category_asc" -> Sort.by(Sort.Direction.ASC, "category").and(Sort.by(Sort.Direction.ASC, "name"));
            case "category,desc", "category_desc" -> Sort.by(Sort.Direction.DESC, "category").and(Sort.by(Sort.Direction.ASC, "name"));
            case "newest", "createdat,desc" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.ASC, "name");
        };
    }
}

