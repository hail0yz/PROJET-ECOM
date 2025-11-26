package com.ecom.bookService.mapper;

import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.dto.CategoryDTO;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.BookInventory;
import com.ecom.bookService.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookMapperTest {

    private CategoryMapper categoryMapper;

    private BookMapper bookMapper;

    @BeforeEach
    void setUp() {
        categoryMapper = mock(CategoryMapper.class);
        bookMapper = new BookMapper(categoryMapper);
    }

    @Test
    void mapToDTO_null_returnsNull() {
        assertNull(bookMapper.mapToDTO(null));
    }

    @Test
    void mapToDTO_mapsFields_andComputesStock() {
        Category category = Category.builder()
                .categoryId(3L)
                .categoryName("Sci-Fi")
                .build();

        CategoryDTO catDto = CategoryDTO.builder().id(3L).name("Sci-Fi").build();
        when(categoryMapper.mapToDTO(category)).thenReturn(catDto);

        Book book = Book.builder()
                .bookId(42L)
                .category(category)
                .title("The Time Machine")
                .author("H. Wells")
                .price(new BigDecimal("12.50"))
                .summary("A classic")
                .isbn10("1234567890")
                .isbn13("123-1234567890")
                .thumbnail("/thumb.png")
                .publishedYear(1895)
                .numPages(200)
                .build();

        BookInventory inv = BookInventory.builder()
                .availableQuantity(10)
                .reservedQuantity(2)
                .build();

        book.setInventory(inv);

        BookDTO dto = bookMapper.mapToDTO(book);

        assertEquals(42L, dto.getId());
        assertEquals("The Time Machine", dto.getTitle());
        assertEquals("H. Wells", dto.getAuthor());
        assertEquals(new BigDecimal("12.50"), dto.getPrice());
        assertEquals("1234567890", dto.getIsbn10());
        assertEquals("123-1234567890", dto.getIsbn13());
        assertEquals("/thumb.png", dto.getThumbnail());
        assertEquals(1895, dto.getPublishedYear());
        assertEquals(200, dto.getNumPages());
        // stock = available - reserved = 8
        assertEquals(8, dto.getStock());
        assertEquals(catDto, dto.getCategory());
    }

}
