package com.ecom.bookService.service;

import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.dto.BookFilter;
import com.ecom.bookService.dto.BookStatsResponse;
import com.ecom.bookService.dto.BulkBookValidationRequest;
import com.ecom.bookService.dto.BulkBookValidationResponse;
import com.ecom.bookService.dto.CreateBookRequest;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.CategoryName;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import org.springframework.data.domain.Page;

public interface BookService {
    public void saveBook(Book book);
    public List<Book> getAllBooks();
    public List<Book> getAllBooksByTitle(String title);
    public BookDTO getBookById(Long id);
    public List<Book> getAllBooksByCategory(String categoryEnum);
    Page<BookDTO> getPagedBooks(BookFilter filter, int page, int size);
    BulkBookValidationResponse validateProducts(BulkBookValidationRequest request);
    Long createBook(CreateBookRequest request, MultipartFile image);
    BookStatsResponse getBookStats();
}
