package com.ecom.bookService.repository;

import com.ecom.bookService.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.bookService.model.BookInventory;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {

    List<BookInventory> findByBookTitle(String title);

    boolean existsByBook(Book book);

    Optional<BookInventory> findByBookBookId(Long bookId);

}
