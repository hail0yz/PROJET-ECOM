package com.ecom.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.order.model.Order;

@Repository
public interface OrderRepo extends JpaRepository<Order, UUID> {

    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    Optional<Order> findByIdAndCustomerId(UUID orderId, String customerId);

}
