package dev.midnightcoder.cafe.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.products WHERE c.id = :id")
    Optional<Cart> findCartById(@Param("id") UUID id);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.products WHERE c.customerId = :customerId")
    Optional<Cart> findByCustomerId(@Param("customerId") UUID customerId);
}