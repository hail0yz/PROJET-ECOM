package org.ecom.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.ecom.cart.model.Cart;
import org.ecom.cart.model.CartStatus;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(String userId);

    Optional<Cart> findByUserIdAndStatus(String userId, CartStatus status);

    boolean existsByIdAndUserIdAndStatus(Long id, String userId, CartStatus status);

    boolean existsByUserIdAndStatus(String userId, CartStatus status);

}
