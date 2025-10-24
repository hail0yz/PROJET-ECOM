package com.ecom.bookService.service;

import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.CategoryName;

import java.util.List;

public interface BookService {
    public void saveBook(Book book);
    public List<Book> getAllBooks();
    public List<Book> getAllBooksByTitle(String title);
    public Book getBookById(Long id);
    public List<Book> getAllBooksByCategory(CategoryName categoryEnum);
}
