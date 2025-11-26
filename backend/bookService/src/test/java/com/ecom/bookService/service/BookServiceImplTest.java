package com.ecom.bookService.service;

import com.ecom.bookService.dto.BulkBookValidationRequest;
import com.ecom.bookService.dto.BulkBookValidationResponse;
import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.mapper.BookMapper;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.BookInventory;
import com.ecom.bookService.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book1;

    @BeforeEach
    void setUp() {
        book1 = Book.builder()
                .bookId(1L)
                .title("Title 1")
                .price(new BigDecimal("9.99"))
                .build();

        BookInventory inv = BookInventory.builder().availableQuantity(5).reservedQuantity(1).build();
        book1.setInventory(inv);
    }

    @Test
    void getBookById_returnsDTO_whenFound() {
        BookDTO dto = BookDTO.builder().id(1L).title("Title 1").build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookMapper.mapToDTO(book1)).thenReturn(dto);

        var result = bookService.getBookById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void validateProducts_missingId_marksInvalid() {
        var req = new BulkBookValidationRequest(List.of(new BulkBookValidationRequest.BookValidationInput(10L, 2)));

        when(bookRepository.findByBookIdIn(List.of(10L))).thenReturn(List.of());

        var res = bookService.validateProducts(req);

        assertFalse(res.isValid());
        assertThat(res.getItems()).hasSize(1);
        assertThat(res.getItems().get(0).isExists()).isFalse();
    }

    @Test
    void validateProducts_insufficientStock_marksInvalid() {
        Book b = Book.builder().bookId(2L).title("T2").price(new BigDecimal("5.00")).build();
        BookInventory inv = BookInventory.builder().availableQuantity(1).reservedQuantity(0).build();
        b.setInventory(inv);

        var req = new BulkBookValidationRequest(List.of(new BulkBookValidationRequest.BookValidationInput(2L, 2)));

        when(bookRepository.findByBookIdIn(List.of(2L))).thenReturn(List.of(b));

        var res = bookService.validateProducts(req);

        assertFalse(res.isValid());
        assertThat(res.getItems()).hasSize(1);
        assertThat(res.getItems().get(0).isExists()).isTrue();
    }

    @Test
    void validateProducts_allGood_marksValid() {
        Book b = Book.builder().bookId(3L).title("T3").price(new BigDecimal("7.00")).build();
        BookInventory inv = BookInventory.builder().availableQuantity(5).reservedQuantity(0).build();
        b.setInventory(inv);

        var req = new BulkBookValidationRequest(List.of(new BulkBookValidationRequest.BookValidationInput(3L, 2)));

        when(bookRepository.findByBookIdIn(List.of(3L))).thenReturn(List.of(b));

        var res = bookService.validateProducts(req);

        assertTrue(res.isValid());
        assertThat(res.getItems()).hasSize(1);
        assertThat(res.getItems().get(0).isExists()).isTrue();
    }

}
