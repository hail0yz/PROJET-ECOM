package com.ecom.bookService.Controller;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.dto.BookFilter;
import com.ecom.bookService.dto.BulkBookValidationRequest;
import com.ecom.bookService.dto.BulkBookValidationResponse;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.CategoryName;
import com.ecom.bookService.service.BookService;

@RestController
@RequestMapping("api/v1/books")
//@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class BookController {

    @Autowired
    private BookService bookService;


    /**
     * GET /api/v1/books
     *      => example with a filter: GET /api/v1/books?search=
     *
     * Returns a list of books regarding optional filters
     *
     * @param search Optional keyword to search in a book's title, summary or author
     * @param minPrice
     * @param maxPrice
     * @param categoryId Optional category ID to filter books belonging to a specific category
     * @param direction
     * @param sortBy
     * @param page Optional page number, with 0 the default value (ie the first page)
     * @param size Optional page size (ie number of books per page), with 10 the default value
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
     *
     * Returns a list of books whose title matches the one given in parameter
     *
     * @param title A title
     * @return A ResponseEntity containing all the books whose title correponds to the one given in parameter in its body
     */
    @GetMapping("title/{title}")
    public ResponseEntity<?> getAllBooksByTitle(@PathVariable String title) {
        List<Book> books = bookService.getAllBooksByTitle(title);

        if(books.isEmpty()) {
            return new ResponseEntity<>("Nothing found for " + title, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(books);
    }


    /**
     * GET /api/v1/books/:id
     *
     * Returns a book whose id matches the one given in parameter
     *
     * @param id The id of a book
     * @return A ResponseEntity containing in its body the book whose id is the one given in parameter. The body will contain a string error if the id corresponds to book.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        if(book == null) {
            return new ResponseEntity<>("Nothing found for id book " + id, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(book);
    }


    /**
     * GET /api/v1/category/:categoryName
     *
     * Returns a list of books whose category name matches the one given in parameter
     *
     * @param categoryName The name of a category
     * @return A ResponseEntity containing in its body a list of all the books whose category correspond to the one given in parameter. The body will contain a string error if the category corresponds to nothing or no books.
     */
    @GetMapping("category/{categoryName}")
    public ResponseEntity<?> getAllBooksByCategory(@PathVariable String categoryName) {

        List<Book> books = bookService.getAllBooksByCategory(categoryName);

        if(books.isEmpty()) {
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

}
