package com.ecom.bookService.service;

import java.math.BigDecimal;

import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.Category;
import com.ecom.bookService.model.CategoryName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/*
    This class allows to populate the database.
    For now, we put datas manually.
 */

@Component
public class DatabasePopulator implements CommandLineRunner {

    private BookService bookService;
    private CategoryService categoryService;

    public DatabasePopulator(BookService bookService,  CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) throws Exception {
        Category category1 = new Category();
        category1.setCategoryName("CategoryName.MANGA");
        categoryService.saveCategory(category1);

        Category category2 = new Category();
        category2.setCategoryName("CategoryName.THRILLER");
        categoryService.saveCategory(category2);



        Book book1 = new Book();
        book1.setTitle("Les trois petits cochons");
        book1.setAuthor("Charles Perrault");
        book1.setPrice(BigDecimal.valueOf(12.34));
        book1.setStock(10);
        book1.setSummary("C'est l'histoire de trois petits cochons...");
        book1.setCategory(category1);
        //bookService.saveBook(book1);

        Book book2 = new Book();
        book2.setTitle("Les trois petits cochons saison 2");
        book2.setAuthor("Charles Perrault");
        book2.setPrice(BigDecimal.valueOf(23.45));
        book2.setStock(10);
        book2.setSummary("C'est l'histoire de trois petits cochons saison 2...");
        //book2.setCategory(category2);

        Book book3 = new Book();
        book3.setTitle("Les trois petits cochons saisson 3");
        book3.setAuthor("Charles Perrault");
        book3.setPrice(BigDecimal.valueOf(34.56));
        book3.setStock(10);
        book3.setSummary("C'est l'histoire de trois petits cochons saison 3...");
        book3.setCategory(category1);
        //bookService.saveBook(book3);
    }
}
