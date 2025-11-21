package com.ecom.bookService.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.CategoryName;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) = LOWER(:title)")
    List<Book> findByTitle(@Param("title") String title);

    @Query("SELECT b FROM Book b WHERE LOWER(b.category.categoryName) = LOWER(:categoryName)")
    List<Book> findByCategory(@Param("categoryName") String categoryName);

    List<Book> findByBookIdIn(List<Long> ids);

    boolean existsByIsbn13(String isbn13);

}
