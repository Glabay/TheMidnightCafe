package dev.midnightcoder.cafe.customers;

import dev.midnightcoder.cafe.customers.api.CustomerCreationRequest;
import dev.midnightcoder.cafe.customers.api.CustomerDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
@RestController
@RequestMapping("/api/v1/customers")
class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService service;

    CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable UUID id) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> {
                var dto = service.findByUserId(id);
                return dto.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound()
                        .build());
            });
    }

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerCreationRequest request,
                                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Invalid customer data: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.registerNewCustomer(request));
    }
}
