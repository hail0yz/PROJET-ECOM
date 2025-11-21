package com.ecom.bookService.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.dto.BookFilter;
import com.ecom.bookService.dto.BulkBookValidationRequest;
import com.ecom.bookService.dto.BulkBookValidationResponse;
import com.ecom.bookService.mapper.BookMapper;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.CategoryName;
import com.ecom.bookService.repository.BookRepository;
import com.ecom.bookService.util.BookSpecificationUtils;
import static java.util.Collections.emptySet;
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
    public List<Book> getAllBooksByCategory(CategoryName categoryEnum) {
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

}
