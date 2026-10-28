package dev.midnightcoder.cafe.customers;

import dev.midnightcoder.cafe.customers.api.CustomerDto;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
interface CustomerToDto {

    default CustomerDto convert(Customer customer) {
        return new CustomerDto(
            customer.getId(),
            customer.getUserId(),
            customer.getCartId(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail(),
            customer.getPhoneNumber(),
            customer.getPreferredName(),
            customer.getBirthday(),
            customer.getLoyaltyPoints(),
            customer.getMarketingOptIn(),
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }
}
