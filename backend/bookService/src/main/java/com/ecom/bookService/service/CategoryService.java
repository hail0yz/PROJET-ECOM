package com.ecom.bookService.service;

import java.util.List;
import java.util.Optional;

import com.ecom.bookService.dto.CategoryDTO;
import com.ecom.bookService.model.Category;

public interface CategoryService {
    void saveCategory(Category category);
    Optional<Category> getByCategoryName(String categoryName);
    List<CategoryDTO> getAllCategories();
}
