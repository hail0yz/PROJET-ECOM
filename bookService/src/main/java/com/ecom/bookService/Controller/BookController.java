package com.ecom.bookService.Controller;

import com.ecom.bookService.model.Book;
import com.ecom.bookService.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("{title}")
    public ResponseEntity<?> getAllBooksByTitle(@PathVariable String title) {
        List<Book> books = bookService.getAllBooksByTitle(title);

        if(books.isEmpty()) {
            return new ResponseEntity<>("Nothing found for " + title, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(books);
    }
}
