package com.ecom.bookService.mapper;

import org.springframework.stereotype.Component;

import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.model.Book;
import lombok.RequiredArgsConstructor;

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
                .thumbnail(book.getThumbnail())
                .build();
    }

}
