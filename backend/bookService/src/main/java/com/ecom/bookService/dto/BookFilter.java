package com.ecom.bookService.dto;

import java.math.BigDecimal;

import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import lombok.Builder;

@Builder
public record BookFilter(
        String search,
        Long categoryId,
        Sort.Direction direction,
        BookSortBy sortBy,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {

    public static BookFilter empty() {
        return BookFilter.builder()
                .direction(Sort.Direction.DESC)
                .sortBy(BookSortBy.PUBLISH_DATE)
                .build();
    }

    public enum BookSortBy {
        TITLE("title"),
        PUBLISH_DATE("createdAt");

        private final String attribute;

        BookSortBy(String attribute) {
            Assert.isTrue(attribute != null && !attribute.isBlank(), () -> "Attribute must be not null");
            this.attribute = attribute;
        }

        public String attribute() {
            return attribute;
        }

    }

}
