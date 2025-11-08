package com.ecom.bookService.mapper;

import org.springframework.stereotype.Component;

import com.ecom.bookService.dto.CategoryDTO;
import com.ecom.bookService.model.Category;

@Component
public class CategoryMapper {

    public CategoryDTO mapToDTO(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryDTO.builder()
                .id(category.getCategoryId())
                .name(category.getCategoryName())
                .description(category.getDescription())
                .image(category.getImage())
                .build();
    }

}
