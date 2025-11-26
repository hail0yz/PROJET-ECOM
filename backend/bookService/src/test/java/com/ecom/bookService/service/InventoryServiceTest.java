package com.ecom.bookService.service;

import com.ecom.bookService.dto.ReservationResult;
import com.ecom.bookService.exception.EntityNotFoundException;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.BookInventory;
import com.ecom.bookService.model.ReservationStatus;
import com.ecom.bookService.model.StockReservation;
import com.ecom.bookService.model.StockReservationItem;
import com.ecom.bookService.repository.BookInventoryRepository;
import com.ecom.bookService.repository.BookRepository;
import com.ecom.bookService.repository.StockReservationItemRepository;
import com.ecom.bookService.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    StockReservationRepository stockReservationRepository;

    @Mock
    StockReservationItemRepository stockReservationItemRepository;

    @Mock
    BookRepository bookRepository;

    @Mock
    BookInventoryRepository bookInventoryRepository;

    @InjectMocks
    InventoryService inventoryService;

    private Book book;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .bookId(1L)
                .title("B1")
                .price(new BigDecimal("3.50"))
                .build();
        BookInventory inv = BookInventory.builder().availableQuantity(5).reservedQuantity(0).build();
        book.setInventory(inv);
    }

    @Test
    void reserveStock_productNotFound_returnsProductNotFound() {
        when(bookRepository.findByBookIdIn(List.of(99L))).thenReturn(List.of());

        var result = inventoryService.reserveStock("order-1", Map.of(99L, 1));

        assertThat(result.status()).isEqualTo(ReservationResult.ReservationStatus.PRODUCT_NOT_FOUND);
        assertThat(result.success()).isFalse();
    }

    @Test
    void reserveStock_success_reservesAndReturnsReservationId() {
        when(bookRepository.findByBookIdIn(List.of(1L))).thenReturn(List.of(book));
        when(stockReservationRepository.findByOrderId("order-2")).thenReturn(Optional.empty());

        StockReservation savedReservation = StockReservation.builder()
                .id(777L)
                .orderId("order-2")
                .status(ReservationStatus.RESERVED)
                .expiresAt(Instant.now())
                .build();

        when(bookRepository.saveAll(any())).thenReturn(List.of(book));
        when(stockReservationRepository.save(any())).thenReturn(savedReservation);

        var result = inventoryService.reserveStock("order-2", Map.of(1L, 2));

        assertThat(result.success()).isTrue();
        assertThat(result.reservationId()).isEqualTo(777L);
        verify(stockReservationItemRepository).saveAll(any());
    }

    @Test
    void confirmReservation_notFound_throws() {
        when(stockReservationRepository.findByOrderId("ordX")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> inventoryService.confirmReservation("ordX"));
    }

    @Test
    void confirmReservation_success_confirmsAndSaves() {
        // prepare reservation with one item
        StockReservation reservation = StockReservation.builder()
                .id(5L)
                .orderId("ord-5")
                .status(ReservationStatus.RESERVED)
                .build();

        StockReservationItem item = StockReservationItem.builder()
                .book(book)
                .reservedQuantity(2)
                .reservation(reservation)
                .build();

        reservation.setItems(List.of(item));

        when(stockReservationRepository.findByOrderId("ord-5")).thenReturn(Optional.of(reservation));
        // ensure inventory reflects reserved state before confirmation
        book.getInventory().setReservedQuantity(item.getReservedQuantity());

        when(bookRepository.saveAll(any())).thenReturn(List.of(book));

        inventoryService.confirmReservation("ord-5");

        verify(bookRepository).saveAll(any());
        verify(stockReservationRepository).save(any());
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void releaseReservation_notFound_throws() {
        when(stockReservationRepository.findByOrderId("ordY")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> inventoryService.releaseReservation("ordY"));
    }

    @Test
    void releaseReservation_notReserved_skipsSave() {
        StockReservation reservation = StockReservation.builder()
                .id(8L)
                .orderId("ord-8")
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(stockReservationRepository.findByOrderId("ord-8")).thenReturn(Optional.of(reservation));

        inventoryService.releaseReservation("ord-8");

        verify(bookRepository, never()).saveAll(any());
        verify(stockReservationRepository, never()).save(any());
    }

    @Test
    void releaseReservation_success_releasesAndSaves() {
        StockReservation reservation = StockReservation.builder()
                .id(9L)
                .orderId("ord-9")
                .status(ReservationStatus.RESERVED)
                .build();

        StockReservationItem item = StockReservationItem.builder()
                .book(book)
                .reservedQuantity(1)
                .reservation(reservation)
                .build();

        reservation.setItems(List.of(item));

        when(stockReservationRepository.findByOrderId("ord-9")).thenReturn(Optional.of(reservation));

        // ensure inventory reflects reserved state before release
        book.getInventory().setReservedQuantity(item.getReservedQuantity());

        when(bookRepository.saveAll(any())).thenReturn(List.of(book));

        inventoryService.releaseReservation("ord-9");

        verify(bookRepository).saveAll(any());
        verify(stockReservationRepository).save(any());
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RELEASED);
    }

}
