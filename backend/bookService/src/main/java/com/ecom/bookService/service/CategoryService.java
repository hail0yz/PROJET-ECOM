package com.ecom.bookService.service;

import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.Category;
import com.ecom.bookService.model.CategoryName;

import java.util.List;

public interface CategoryService {
    public void saveCategory(Category category);
    public List<Book> getByCategoryName(CategoryName categoryName);
    public List<Category> getAllCategories();
}
