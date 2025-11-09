package com.ecom.bookService.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.ecom.bookService.dto.ReservationResult;
import com.ecom.bookService.exception.EntityNotFoundException;
import com.ecom.bookService.exception.InsufficientStockException;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.ReservationStatus;
import com.ecom.bookService.model.StockReservation;
import com.ecom.bookService.model.StockReservationItem;
import com.ecom.bookService.repository.BookRepository;
import com.ecom.bookService.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private static final Duration RESERVATION_DURATION_EXPIRE = Duration.ofHours(24);

    private final StockReservationRepository stockReservationRepository;

    private final BookRepository bookRepository;

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    public ReservationResult reserveStock(String orderId, Map<Long, Integer> products) {
        log.info("Stock reservation - order: {}, products: {}", orderId, products);

        try {
            List<Book> books = bookRepository.findByBookIdIn(new ArrayList<>(products.keySet()));

            if (books.size() != products.size()) {
                List<Long> bookIds = books.stream()
                        .map(Book::getBookId)
                        .toList();

                List<Long> notFound = products.keySet().stream()
                        .filter(bookIds::contains)
                        .toList();

                return ReservationResult.productNotFound(notFound);
            }

            boolean canReserve = books.stream().allMatch(book -> book.canReserve(products.get(book.getBookId())));
            if (!canReserve) {
                return ReservationResult.failed(orderId, null, "no quantity !");
            }

            Optional<StockReservation> existingReservation = stockReservationRepository.findByOrderId(orderId);

            if (existingReservation.isPresent()) {
                return ReservationResult.alreadyReserved(existingReservation.get().getId());
            }

            for (Book book : books) {
                Integer quantity = products.get(book.getBookId());
                book.reserve(quantity);
            }

            var savedBooks = bookRepository.saveAll(books);

            List<StockReservationItem> reservationItems = savedBooks.stream()
                    .map(book -> StockReservationItem.builder()
                            .book(book)
                            .reservedQuantity(products.get(book.getBookId()))
                            .build())
                    .toList();

            StockReservation reservation = StockReservation.builder()
                    .orderId(orderId)
                    .items(reservationItems)
                    .expiresAt(Instant.now().plus(RESERVATION_DURATION_EXPIRE))
                    .build();

            StockReservation savedReservation = stockReservationRepository.save(reservation);

            // TODO publish {new StockReserved(orderId, products)}

            return ReservationResult.success(savedReservation.getId());
        }
        catch (InsufficientStockException e) {
            return ReservationResult.failed("Insufficient stock: " + e.getMessage());
        }
        catch (Exception e) {
            return ReservationResult.failed("Internal error: " + e.getMessage());
        }
    }

    public void confirmReservation(String orderId) {
        log.info("Confirming reservation - Order: {}", orderId);

        StockReservation reservation = stockReservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Reservation not found. orderId=%s", orderId)));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("Not possible to confirm an inactive reservation: " + reservation.getStatus());
        }

        List<Book> books = reservation.getItems().stream()
                .map(item -> {
                    item.getBook().confirmReservation(item.getReservedQuantity());
                    return item.getBook();
                })
                .toList();

        bookRepository.saveAll(books);

        reservation.confirm();
        stockReservationRepository.save(reservation);

        // TODO publish event : StockConfirmed / StockUpdated

        log.info("Reservation confirmed successfully: {}", reservation.getId());
    }

    public void releaseReservation(String orderId) {
        log.info("Libération de réservation - Order: {}", orderId);

        StockReservation reservation = stockReservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            log.warn("Release skipped: reservation [id={}] is not active (current status={})",
                    reservation.getId(), reservation.getStatus());
            return;
        }

        List<Book> books = reservation.getItems().stream()
                .map(item -> {
                    item.getBook().cancelReservation(item.getReservedQuantity());
                    return item.getBook();
                })
                .toList();

        bookRepository.saveAll(books);

        reservation.release();
        stockReservationRepository.save(reservation);

        // TODO publish event : StockReservationCancelledEvent

        log.info("Reservation successfully released [reservationId:{}]", reservation.getId());
    }

}