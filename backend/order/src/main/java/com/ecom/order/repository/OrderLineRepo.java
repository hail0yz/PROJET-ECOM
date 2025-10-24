package com.ecom.order.repository;

import com.ecom.order.model.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderLineRepo extends JpaRepository<OrderLine, UUID> {
    List<OrderLine> findAllByOrderId(UUID orderId);
}
