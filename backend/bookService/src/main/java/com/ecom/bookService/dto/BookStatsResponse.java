package com.ecom.bookService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookStatsResponse {

    private long totalBooks;

    private long totalCategories;

}
