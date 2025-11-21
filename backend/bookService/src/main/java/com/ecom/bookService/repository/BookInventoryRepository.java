package com.ecom.bookService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.bookService.model.BookInventory;

@Repository
public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {
}
