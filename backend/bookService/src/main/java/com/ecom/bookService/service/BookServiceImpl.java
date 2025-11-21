package com.ecom.bookService.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.dto.BookFilter;
import com.ecom.bookService.mapper.BookMapper;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.CategoryName;
import com.ecom.bookService.repository.BookRepository;
import com.ecom.bookService.util.BookSpecificationUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public List<Book> getAllBooksByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    @Override
    public BookDTO getBookById(Long id) {
        return bookRepository.findById(id)
                .map(bookMapper::mapToDTO)
                .orElse(null);
    }

    @Override
    public List<Book> getAllBooksByCategory(String categoryEnum) {
        return bookRepository.findByCategory(categoryEnum);
    }

    @Override
    public Page<BookDTO> getPagedBooks(BookFilter filter, int page, int size) {
        return bookRepository.findAll(BookSpecificationUtils.filter(filter), PageRequest.of(page, size))
                .map(bookMapper::mapToDTO);
    }

}
