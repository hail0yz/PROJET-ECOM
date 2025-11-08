package com.ecom.bookService.Controller;

import com.ecom.bookService.dto.BookDTO;
import com.ecom.bookService.dto.BookFilter;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.CategoryName;
import com.ecom.bookService.service.BookService;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/v1/books")
@CrossOrigin(origins = "http://localhost:4200")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * GET /api/v1/books
     *
     * @return List containing all books
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
     * GET /api/v1/books/id/:id
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
     * @param categoryName The name of a category
     * @return A ResponseEntity containing in its body a list of all the books whose category correspond to the one given in parameter. The body will contain a string error if the category corresponds to nothing or no books.
     */
    @GetMapping("category/{categoryName}")
    public ResponseEntity<?> getAllBooksByCategory(@PathVariable String categoryName) {

        // convert to enum types
        CategoryName categoryEnum;
        try {
            categoryEnum = CategoryName.valueOf(categoryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Category " + categoryName + " doesn't exist.", HttpStatus.BAD_REQUEST);
        }

        List<Book> books = bookService.getAllBooksByCategory(categoryEnum);

        if(books.isEmpty()) {
            return new ResponseEntity<>("Nothing found for " + categoryName, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(books);
    }

}
