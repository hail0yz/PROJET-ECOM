package com.ecom.bookService.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.dto.BookFilter;
import com.ecom.bookService.dto.BulkBookValidationRequest;
import com.ecom.bookService.dto.BulkBookValidationResponse;
import com.ecom.bookService.dto.CreateBookRequest;
import com.ecom.bookService.dto.UpdateBookRequest;
import com.ecom.bookService.mapper.BookMapper;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.BookInventory;
import com.ecom.bookService.model.Category;
import com.ecom.bookService.repository.BookRepository;
import com.ecom.bookService.repository.CategoryRepository;
import com.ecom.bookService.util.BookSpecificationUtils;
import static java.util.Collections.emptySet;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final ImageService imageService;
    private final CategoryRepository categoryRepository;

    public Long addBook(BookDTO bookDTO) {

        Book book=bookMapper.toBook(bookDTO);

        if(bookRepository.existsById(book.getBookId())){
            throw new EntityExistsException("Book already exists");
        }
        return bookRepository.save(book).getBookId();
    }

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

    @Transactional
    public Book updateBook(Long id, UpdateBookRequest request, MultipartFile image) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        book.setTitle(request.getTitle());
        book.setSummary(request.getSummary());
        book.setPrice(request.getPrice());
        book.setAuthor(request.getAuthor());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        book.setCategory(category);

        if (image != null && !image.isEmpty()) {
            String thumbnail = imageService.uploadImage(image);
            book.setThumbnail(thumbnail);
        }

        return bookRepository.save(book);
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

    @Override
    public BulkBookValidationResponse validateProducts(BulkBookValidationRequest request) {
        Map<Long, Integer> booksIdsQuantities = request.items().stream()
                .collect(Collectors.toMap(
                        BulkBookValidationRequest.BookValidationInput::bookId,
                        BulkBookValidationRequest.BookValidationInput::quantity
                ));

        List<Long> ids = new ArrayList<>(booksIdsQuantities.keySet());

        List<Book> books = bookRepository.findByBookIdIn(ids);

        Set<Long> foundIds = books.stream().map(Book::getBookId).collect(Collectors.toSet());
        Set<Long> missingIds = ids.size() != books.size()
                ? ids.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toSet())
                : emptySet();

        List<BulkBookValidationResponse.BookValidationResult> results = new ArrayList<>();

        for (Long missingId : missingIds) {
            results.add(BulkBookValidationResponse.BookValidationResult.builder()
                    .bookId(missingId)
                    .exists(false)
                    .build());
        }

        boolean valid = missingIds.isEmpty();

        for (Book book : books) {
            int quantityRequested = booksIdsQuantities.get(book.getBookId());
            valid = valid && book.getInventory().canReserve(booksIdsQuantities.get(book.getBookId()));
            results.add(BulkBookValidationResponse.BookValidationResult.builder()
                    .bookId(book.getBookId())
                    .title(book.getTitle())
                    .image(book.getThumbnail())
                    .exists(true)
                    .requestedQuantity(quantityRequested)
                    .availableQuantity(book.getInventory().getAvailableQuantity() - book.getInventory().getReservedQuantity())
                    .price(book.getPrice())
                    .build());
        }

        return BulkBookValidationResponse.builder()
                .valid(valid)
                .items(results)
                .build();
    }

    @Transactional
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Long createBook(CreateBookRequest request, MultipartFile image) {
        if (bookRepository.existsByIsbn10(request.isbn10())) {
            throw new EntityExistsException("Book with ISBN-10 " + request.isbn10() + " already exists");
        }

        if (request.isbn13() != null && bookRepository.existsByIsbn13(request.isbn13())) {
            throw new EntityExistsException("Book with ISBN-13 " + request.isbn13() + " already exists");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        String thumbnail = imageService.uploadImage(image);

        BookInventory inventory = BookInventory.builder()
                .availableQuantity(request.initialStock())
                .reservedQuantity(0)
                .minimumStockLevel(10)
                .build();

        Book book = Book.builder()
                .title(request.title())
                .summary(request.description())
                .category(category)
                .author(request.author())
                .thumbnail(thumbnail)
                .inventory(inventory)
                .price(request.price())
                .build();

        inventory.setBook(book);

        Book saved = bookRepository.save(book);

        return saved.getBookId();
    }

}
