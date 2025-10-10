package com.ecom.bookService.service;

import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.Category;
import com.ecom.bookService.model.CategoryName;
import com.ecom.bookService.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public List<Book> findByCategoryName(CategoryName categoryName) {
        return categoryRepository.findByCategoryName(categoryName);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
