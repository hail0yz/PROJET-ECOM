package com.ecom.bookService.Controller;

import com.ecom.bookService.model.Category;
import com.ecom.bookService.repository.CategoryRepository;
import com.ecom.bookService.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
@CrossOrigin(origins = "http://localhost:4200")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    public List<Category> getAllCategories() { return categoryService.getAllCategories(); }
}
