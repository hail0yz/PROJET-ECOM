package com.ecom.bookService.service;

import com.ecom.bookService.dto.CategoryDTO;
import com.ecom.bookService.mapper.CategoryMapper;
import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.Category;
import com.ecom.bookService.model.CategoryName;
import com.ecom.bookService.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public Optional<Category> getByCategoryName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "categoryName"))
                .stream()
                .map(categoryMapper::mapToDTO)
                .toList();
    }

}
