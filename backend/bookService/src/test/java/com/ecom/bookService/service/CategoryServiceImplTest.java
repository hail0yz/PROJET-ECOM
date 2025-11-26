package com.ecom.bookService.service;

import com.ecom.bookService.dto.CategoryDTO;
import com.ecom.bookService.mapper.CategoryMapper;
import com.ecom.bookService.model.Category;
import com.ecom.bookService.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    CategoryMapper categoryMapper;

    @InjectMocks
    CategoryServiceImpl categoryService;

    @Test
    void getByCategoryName_returnsOptional() {
        Category cat = Category.builder().categoryId(2L).categoryName("F").build();
        when(categoryRepository.findByCategoryName("F")).thenReturn(Optional.of(cat));

        var opt = categoryService.getByCategoryName("F");

        assertThat(opt).isPresent();
        assertThat(opt.get().getCategoryId()).isEqualTo(2L);
    }

    @Test
    void getAllCategories_mapsToDTOs() {
        Category cat = Category.builder().categoryId(3L).categoryName("X").build();
        CategoryDTO dto = CategoryDTO.builder().id(3L).name("X").build();

        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of(cat));
        when(categoryMapper.mapToDTO(cat)).thenReturn(dto);

        var list = categoryService.getAllCategories();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(3L);
    }

}
