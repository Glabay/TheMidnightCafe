package dev.midnightcoder.cafe.customers.api;

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
public interface CustomerLookup {
    Optional<CustomerDto> findByUserId(UUID userId);
    Optional<CustomerDto> findById(UUID id);
    Optional<CustomerDto> findByEmail(String email);

    List<CustomerDto> findAll();
    Long count();
}
