package com.ecom.bookService.service;

import com.ecom.bookService.model.Book;

import java.util.List;

public interface BookService {
    public void saveBook(Book book);
    public List<Book> getAllBooks();
    public List<Book> getAllBooksByTitle(String title);
}
