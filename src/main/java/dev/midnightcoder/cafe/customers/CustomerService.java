package dev.midnightcoder.cafe.customers;

import dev.midnightcoder.cafe.customers.api.CustomerCreationRequest;
import dev.midnightcoder.cafe.customers.api.CustomerDto;
import dev.midnightcoder.cafe.customers.api.CustomerLookup;
import dev.midnightcoder.cafe.products.api.CartOperations;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

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
@Service
public class CustomerService implements CustomerLookup, CustomerToDto {
    private final CustomerRepository repository;
    private final CartOperations cartOperations;

    CustomerService(CustomerRepository repository, CartOperations cartOperations) {
        this.repository = repository;
        this.cartOperations = cartOperations;
    }

    @Override
    public Optional<CustomerDto> findByUserId(UUID userId) {
        return repository.findByUserId(userId).map(this::convert);
    }

    @Override
    public Optional<CustomerDto> findById(UUID id) {
        return repository.findById(id).map(this::convert);
    }

    @Override
    public Optional<CustomerDto> findByEmail(String email) {
        return repository.findByEmail(email).map(this::convert);
    }

    @Override
    public List<CustomerDto> findAll() {
        return repository.findAll().stream().map(this::convert).toList();
    }

    public CustomerDto registerNewCustomer(@Valid CustomerCreationRequest request) {
        var customer = new Customer();
        customer.setUserId(request.userId());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setPreferredName(request.preferredName());
        customer.setBirthday(request.birthday());
        customer.setLoyaltyPoints(request.loyaltyPoints() != null ? request.loyaltyPoints() : 0);
        customer.setMarketingOptIn(request.marketingOptIn() != null && request.marketingOptIn());
        customer = repository.save(customer);

        var cartId = cartOperations.createCart(customer.getId());
        customer.setCartId(cartId);
        return convert(repository.save(customer));
    }

    public Optional<CustomerDto> update(UUID id, CustomerDto form) {
        return repository.findById(id).map(customer -> {
            if (form.userId() != null) customer.setUserId(form.userId());
            customer.setFirstName(form.firstName());
            customer.setLastName(form.lastName());
            customer.setEmail(form.email());
            customer.setPhoneNumber(form.phoneNumber());
            customer.setPreferredName(form.preferredName());
            customer.setBirthday(form.birthday());
            if (form.loyaltyPoints() != null) customer.setLoyaltyPoints(form.loyaltyPoints());
            if (form.marketingOptIn() != null) customer.setMarketingOptIn(form.marketingOptIn());
            return convert(repository.save(customer));
        });
    }

    public Optional<CustomerDto> update(UUID id, CustomerCreationRequest form) {
        return repository.findById(id).map(customer -> {
            if (form.userId() != null) customer.setUserId(form.userId());
            customer.setFirstName(form.firstName());
            customer.setLastName(form.lastName());
            customer.setEmail(form.email());
            customer.setPhoneNumber(form.phoneNumber());
            customer.setPreferredName(form.preferredName());
            customer.setBirthday(form.birthday());
            if (form.loyaltyPoints() != null) customer.setLoyaltyPoints(form.loyaltyPoints());
            if (form.marketingOptIn() != null) customer.setMarketingOptIn(form.marketingOptIn());
            return convert(repository.save(customer));
        });
    }

    @Override
    public Long count() {
        return repository.countAllBy();
    }

    public void assignCart(UUID customerId, UUID cartId) {
        repository.findById(customerId).ifPresent(customer -> {
            customer.setCartId(cartId);
            repository.save(customer);
        });
    }
}
