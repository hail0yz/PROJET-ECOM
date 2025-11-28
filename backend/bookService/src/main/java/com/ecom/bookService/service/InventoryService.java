package com.ecom.bookService.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.dto.CreateInvetoryExistedBookDto;
import com.ecom.bookService.dto.InventaireDto;
import com.ecom.bookService.dto.InventaireResponseDto;
import com.ecom.bookService.dto.InverntoryCreationDto;
import com.ecom.bookService.dto.ReservationResult;
import com.ecom.bookService.dto.UpdateBookQuantityRequest;
import com.ecom.bookService.exception.EntityNotFoundException;
import com.ecom.bookService.exception.InsufficientStockException;
import com.ecom.bookService.mapper.BookInvetoryMapper;
import com.ecom.bookService.mapper.BookMapper;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.BookInventory;
import com.ecom.bookService.model.ReservationStatus;
import com.ecom.bookService.model.StockReservation;
import com.ecom.bookService.model.StockReservationItem;
import com.ecom.bookService.repository.BookInventoryRepository;
import com.ecom.bookService.repository.BookRepository;
import com.ecom.bookService.repository.StockReservationItemRepository;
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

    private final StockReservationItemRepository stockReservationItemRepository;

    private final BookRepository bookRepository;

    private final BookInventoryRepository bookInventoryRepository;

    private final BookServiceImpl bookServiceImpl;

    private BookMapper bookMapper;

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, backoff = @Backoff(delay = 100))
    public ReservationResult reserveStock(String orderId, Map<Long, Integer> products) {
        log.info("Reserving stock - order: {}, products: {}", orderId, products);

        try {
            List<Book> books = bookRepository.findByBookIdIn(new ArrayList<>(products.keySet()));

            if (books.size() != products.size()) {
                List<Long> bookIds = books.stream()
                        .map(Book::getBookId)
                        .toList();

                List<Long> notFound = products.keySet().stream()
                        .filter(bookIds::contains)
                        .toList();

                ReservationResult result = ReservationResult.productNotFound(notFound);
                log.info("Stock Reservation Failed (cause : productNotFound) : {}", result);
                return result;
            }

            boolean canReserve = books.stream()
                    .allMatch(book -> book.getInventory().canReserve(products.get(book.getBookId())));

            if (!canReserve) {
                ReservationResult result = ReservationResult.failed(orderId, null, "no quantity !");
                log.info("Stock Reservation Failed : {}", result);
                return result;
            }

            Optional<StockReservation> existingReservation = stockReservationRepository.findByOrderId(orderId);

            if (existingReservation.isPresent()) {
                return ReservationResult.alreadyReserved(existingReservation.get().getId());
            }

            for (Book book : books) {
                Integer quantity = products.get(book.getBookId());
                book.getInventory().reserve(quantity);
            }

            var savedBooks = bookRepository.saveAll(books);

            StockReservation reservation = StockReservation.builder()
                    .orderId(orderId)
                    .status(ReservationStatus.RESERVED)
                    .expiresAt(Instant.now().plus(RESERVATION_DURATION_EXPIRE))
                    .build();

            StockReservation savedReservation = stockReservationRepository.save(reservation);

            List<StockReservationItem> reservationItems = savedBooks.stream()
                    .map(book -> StockReservationItem.builder()
                            .book(book)
                            .reservedQuantity(products.get(book.getBookId()))
                            .reservation(savedReservation)
                            .build())
                    .toList();

            stockReservationItemRepository.saveAll(reservationItems);

            // TODO publish {new StockReserved(orderId, products)}

            ReservationResult result = ReservationResult.success(savedReservation.getId());
            log.info("Stock has been reserved successfully : {}", result);
            return result;
        }
        catch (InsufficientStockException e) {
            return ReservationResult.failed("Insufficient stock: " + e.getMessage());
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
                    item.getBook().getInventory().confirmReservation(item.getReservedQuantity());
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
                    item.getBook().getInventory().cancelReservation(item.getReservedQuantity());
                    return item.getBook();
                })
                .toList();

        bookRepository.saveAll(books);

        reservation.release();
        stockReservationRepository.save(reservation);

        // TODO publish event : StockReservationCancelledEvent

        log.info("Reservation successfully released [reservationId:{}]", reservation.getId());
    }

    //get all inventaire
    public List<InventaireResponseDto> getAllInventory() {
        return this.bookInventoryRepository.findAll().stream()
                .map(BookInvetoryMapper::toResponseInv)
                .collect(Collectors.toList());
    }

    //get all inventaire by category
    public List<InventaireResponseDto> findINvertoryByTilte(String tilte) {
        return this.bookInventoryRepository.findByBookTitle(tilte).stream()
                .map(BookInvetoryMapper::toResponseInv)
                .collect(Collectors.toList());
    }

    //get inventaire by id
    public InventaireDto getInvById(Long id) {
        return InventaireDto.toInvetaire(bookInventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found.")));
    }

    //add existed book to an invertory
    @Transactional
    public BookInventory createInventoryForExistingBook(CreateInvetoryExistedBookDto dto) {
        log.info("Creating inventory for existing book - bookId: {}", dto.getBookId());

        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));


        if (bookInventoryRepository.existsByBook(book)) {
            throw new IllegalStateException("This book already has an inventory.");
        }


        BookInventory inventory = BookInventory.builder()
                .book(book)
                .availableQuantity(dto.getAvailableQuantity())
                .minimumStockLevel(dto.getMinimumStockLevel())
                .reservedQuantity(0)
                .build();


        return bookInventoryRepository.save(inventory);
    }

    @Transactional
    public void updateQuantity(Long bookid, UpdateBookQuantityRequest request) {
        log.info("Updating book inventory - bookId: {}, quantity: {}", bookid, request.quantity());
        BookInventory inventory = bookInventoryRepository.findByBookBookId(bookid)
                .orElseThrow(() -> new EntityNotFoundException("Book not found !"));

        if (request.quantity() < inventory.getReservedQuantity()) {
            throw new IllegalArgumentException("Available quantity cannot be less than reserved quantity.");
        }

        inventory.setAvailableQuantity(request.quantity());

        bookInventoryRepository.save(inventory);
    }

    @Retryable(
            retryFor = OptimisticLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public BookInventory addStock(Long bookId, int quantity) {
        log.info("Adding stock - bookId: {}, quantity: {}", bookId, quantity);
        BookInventory inv = bookInventoryRepository.findByBookBookId(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found !"));
        inv.setAvailableQuantity(inv.getAvailableQuantity() + quantity);
        return bookInventoryRepository.save(inv);
    }


}