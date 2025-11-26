package com.ecom.bookService.mapper;

import com.ecom.bookService.dto.CategoryDTO;
import com.ecom.bookService.model.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapper();

    @Test
    void mapToDTO_null_returnsNull() {
        assertNull(mapper.mapToDTO(null));
    }

    @Test
    void mapToDTO_mapsFields() {
        Category category = Category.builder()
                .categoryId(7L)
                .categoryName("Fiction")
                .description("Novels and stories")
                .image("/cat.png")
                .build();

        CategoryDTO dto = mapper.mapToDTO(category);

        assertNotNull(dto);
        assertEquals(7L, dto.getId());
        assertEquals("Fiction", dto.getName());
        assertEquals("Novels and stories", dto.getDescription());
        assertEquals("/cat.png", dto.getImage());
    }

}
