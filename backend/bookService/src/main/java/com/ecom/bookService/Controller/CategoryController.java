package com.ecom.bookService.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.bookService.dto.CategoryDTO;
import com.ecom.bookService.service.CategoryService;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("api/v1/categories")
//@CrossOrigin(origins = "http://localhost:4200")
public class CategoryController {

    private static final String DEFAULT_PAGE_SIZE = "20";
    private static final String DEFAULT_CURRENT_PAGE = "0";

    @Autowired
    private CategoryService categoryService;


    /**
     * GET /api/v1/categories
     *
     * Returns a list of all categories (non-paginated for backward compatibility)
     *
     * @return A ResponseEntity containing all the categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * GET /api/v1/categories/paged
     *
     * Returns a paginated list of categories
     *
     * @param page The page number (0-indexed)
     * @param size The page size
     * @return A ResponseEntity containing the paginated categories
     */
    @GetMapping("/paged")
    public ResponseEntity<Page<CategoryDTO>> getAllCategoriesPaged(
            @Parameter(name = "The current result page requested.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int page,
            @Parameter(name = "The number of results returned per page.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int size) {
        return ResponseEntity.ok(categoryService.getAllCategories(page, size));
    }

}
