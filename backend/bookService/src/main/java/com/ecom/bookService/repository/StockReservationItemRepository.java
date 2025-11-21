package com.ecom.bookService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.bookService.model.StockReservationItem;

@Repository
public interface StockReservationItemRepository extends JpaRepository<StockReservationItem, Long> {
}