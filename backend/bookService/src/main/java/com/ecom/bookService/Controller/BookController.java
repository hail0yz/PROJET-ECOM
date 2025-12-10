package com.ecom.bookService.Controller;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.dto.BookFilter;
import com.ecom.bookService.dto.BulkBookValidationRequest;
import com.ecom.bookService.dto.BulkBookValidationResponse;
import com.ecom.bookService.dto.CreateBookRequest;
import com.ecom.bookService.dto.UpdateBookRequest;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.service.BookService;
import com.ecom.bookService.service.BookServiceImpl;

@RestController
@RequestMapping("api/v1/books")
//@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookServiceImpl bookServiceImpl;


    /**
     * GET /api/v1/books
     * => example with a filter: GET /api/v1/books?search=
     * <p>
     * Returns a list of books regarding optional filters
     *
     * @param search     Optional keyword to search in a book's title, summary or author
     * @param minPrice
     * @param maxPrice
     * @param categoryId Optional category ID to filter books belonging to a specific category
     * @param direction
     * @param sortBy
     * @param page       Optional page number, with 0 the default value (ie the first page)
     * @param size       Optional page size (ie number of books per page), with 10 the default value
     * @return A Page of BookDTO containing the books for the requested page number
     */
    @GetMapping
    public Page<BookDTO> getAllBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Sort.Direction direction,
            @RequestParam(required = false) BookFilter.BookSortBy sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        BookFilter filter = BookFilter.builder()
                .search(search)
                .categoryId(categoryId)
                .direction(direction)
                .sortBy(sortBy)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();

        return bookService.getPagedBooks(filter, page, size);
    }


    /**
     * GET /api/v1/books/title/:title
     * <p>
     * Returns a list of books whose title matches the one given in parameter
     *
     * @param title A title
     * @return A ResponseEntity containing all the books whose title correponds to the one given in parameter in its body
     */
    @GetMapping("title/{title}")
    public ResponseEntity<?> getAllBooksByTitle(@PathVariable String title) {
        List<Book> books = bookService.getAllBooksByTitle(title);

        if (books.isEmpty()) {
            return new ResponseEntity<>("Nothing found for " + title, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(books);
    }


    /**
     * GET /api/v1/books/:id
     * <p>
     * Returns a book whose id matches the one given in parameter
     *
     * @param id The id of a book
     * @return A ResponseEntity containing in its body the book whose id is the one given in parameter. The body will contain a string error if the id corresponds to book.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        if (book == null) {
            return new ResponseEntity<>("Nothing found for id book " + id, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(book);
    }


    /**
     * GET /api/v1/category/:categoryName
     * <p>
     * Returns a list of books whose category name matches the one given in parameter
     *
     * @param categoryName The name of a category
     * @return A ResponseEntity containing in its body a list of all the books whose category correspond to the one given in parameter. The body will contain a string error if the category corresponds to nothing or no books.
     */
    @GetMapping("category/{categoryName}")
    public ResponseEntity<?> getAllBooksByCategory(@PathVariable String categoryName) {

        List<Book> books = bookService.getAllBooksByCategory(categoryName);

        if (books.isEmpty()) {
            return new ResponseEntity<>("Nothing found for " + categoryName, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(books);
    }

    @PostMapping("/validate")
    public ResponseEntity<BulkBookValidationResponse> validateProducts(
            @RequestBody @Valid BulkBookValidationRequest request
    ) {
        BulkBookValidationResponse response = bookService.validateProducts(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/books
     * <p>
     * Creates a new book
     *
     * @param bookDTO The book data to create
     * @return A ResponseEntity containing the created book ID
     */
    //@PostMapping
    public ResponseEntity<Long> createBook(@RequestBody @Valid BookDTO bookDTO) {
        Long bookId = bookServiceImpl.addBook(bookDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
        @PathVariable Long id,
        @RequestPart @Valid UpdateBookRequest request,
        @RequestPart(required = false) MultipartFile image
    ) {
        bookServiceImpl.updateBook(id, request, image);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/v1/books/:id
     * <p>
     * Deletes a book
     *
     * @param id The id of the book to delete
     * @return A ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookServiceImpl.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = {"", "/"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookId> createBook(
            @RequestPart @Valid CreateBookRequest request,
            @RequestPart MultipartFile image
    ) {
        Long bookId = bookService.createBook(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(new BookId(bookId));
    }

    public record BookId(Long id) {
    }

}
