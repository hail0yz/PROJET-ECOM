package com.ecom.bookService.mapper;

import org.springframework.stereotype.Component;

import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.model.Book;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BookMapper {

    private final CategoryMapper categoryMapper;

    public BookDTO mapToDTO(Book book) {
        if (book == null) {
            return null;
        }

        return BookDTO.builder()
                .id(book.getBookId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .summary(book.getSummary())
                .category(categoryMapper.mapToDTO(book.getCategory()))
                .isbn10(book.getIsbn10())
                .isbn13(book.getIsbn13())
                .numPages(book.getNumPages())
                .publishedYear(book.getPublishedYear())
                .price(book.getPrice())
                .thumbnail(book.getThumbnail())
                .stock(book.getInventory().getAvailableQuantity() - book.getInventory().getReservedQuantity())
                .build();
    }

    public Book toBook(BookDTO bookDTO){
        if (bookDTO == null) {
            return null;
        }
        return Book.builder()
                .bookId(bookDTO.getId())
                .title(bookDTO.getTitle())
                .author(bookDTO.getAuthor())
                .isbn10(bookDTO.getIsbn10())
                .isbn13(bookDTO.getIsbn13())
                .summary(bookDTO.getSummary())
                .numPages(bookDTO.getNumPages())
                .publishedYear(bookDTO.getPublishedYear())
                .createdAt(bookDTO.getCreatedAt())
                .stock(bookDTO.getStock())
                .build();
    }


}
