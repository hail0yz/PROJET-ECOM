package com.ecom.bookService.Controller;

import com.ecom.bookService.model.Category;
import com.ecom.bookService.repository.CategoryRepository;
import com.ecom.bookService.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<Category> getAllCategories() { return categoryService.getAllCategories(); }
}
