package com.ecom.order.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecom.order.model.Order;
import com.ecom.order.model.OrderStatus;

@Repository
public interface OrderRepo extends JpaRepository<Order, UUID> {

    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    Optional<Order> findByIdAndCustomerId(UUID orderId, String customerId);

    Optional<Order> findByCartIdAndCustomerId(Long cartId, String customerId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal getTotalRevenue(@Param("status") OrderStatus status);

    default BigDecimal getTotalRevenue() {
        return getTotalRevenue(OrderStatus.CONFIRMED);
    }

}
