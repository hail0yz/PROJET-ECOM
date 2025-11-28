package com.ecom.bookService.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ecom.bookService.dto.*;
import com.ecom.bookService.mapper.BookInvetoryMapper;
import com.ecom.bookService.mapper.BookMapper;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.ecom.bookService.exception.EntityNotFoundException;
import com.ecom.bookService.exception.InsufficientStockException;
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
import jakarta.persistence.OptimisticLockException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private static final Duration RESERVATION_DURATION_EXPIRE = Duration.ofHours(24);

    private final StockReservationRepository stockReservationRepository;

    private final StockReservationItemRepository stockReservationItemRepository;

    private final BookRepository bookRepository;

    @Autowired
    private final BookInventoryRepository bookInventoryRepository;

    @Autowired
    private final BookServiceImpl bookServiceImpl;

    @Autowired
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

   //admin Gestion d'inventaire

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
    public InventaireDto getInvById(Long id){
       return InventaireDto.toInvetaire(bookInventoryRepository.findById(id)
               .orElseThrow(()->new EntityNotFoundException("Inventory not found.")));
    }


    //add book inventory
    @Transactional
    public Long addInventory(InverntoryCreationDto inventaire){
        BookDTO bookDto=BookDTO.builder()
                .title(inventaire.getTitle())
                .author(inventaire.getAuthor())
                .price(inventaire.getPrice())
                .summary(inventaire.getSummary())
                .isbn10(inventaire.getIsbn10())
                .isbn13(inventaire.getIsbn13())
                .thumbnail(inventaire.getThumbnail())
                .publishedYear(inventaire.getPublishedYear())
                .numPages(inventaire.getNumPages())
                .build();
        Long bookid=bookServiceImpl.addBook(bookDto);

        BookInventory inventory = BookInventory.builder()
                .book(bookMapper.toBook(bookServiceImpl.getBookById(bookid)))
                .availableQuantity(inventaire.getAvailableQuantity())
                .minimumStockLevel(inventaire.getMinimumStockLevel())
                .reservedQuantity(0)
                .build();

        return this.bookInventoryRepository.save(inventory).getId();

    }

    //add existed book to an invertory
    @Transactional
    public BookInventory createInventoryForExistingBook(CreateInvetoryExistedBookDto dto) {


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

    //update an inventaire
    @Transactional
    public void updateINventaire(UpdateInventaireDto dto){

        BookInventory inventory=bookInventoryRepository.findById(dto.getInventaireId())
                .orElseThrow(()->new EntityNotFoundException("Inventory not found."));

        if(dto.getBookId()!=null){
            Book book=bookRepository.findById(dto.getBookId()).
                    orElseThrow(()->new EntityNotFoundException("Book not found"));
            if(bookInventoryRepository.existsByBook(book)){
                throw new IllegalStateException("This book already has an inventory.");
            }else {
                inventory.setBook(book);

            }
        }

        inventory.setAvailableQuantity(dto.getAvailableQuantity());
        inventory.setMinimumStockLevel(dto.getMinimumStockLevel());
        inventory.setReservedQuantity(dto.getReservedQuantity());
    }

    @Retryable(
            value = OptimisticLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public BookInventory addStock(Long bookId, int quantity) {
        BookInventory inv = bookInventoryRepository.findByBookId(bookId)
                .orElseThrow(()->new EntityNotFoundException("Inventory not found."));
        inv.setAvailableQuantity(inv.getAvailableQuantity() + quantity);
        return bookInventoryRepository.save(inv);
    }






}