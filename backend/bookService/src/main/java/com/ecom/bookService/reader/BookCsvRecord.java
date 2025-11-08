package com.ecom.bookService.reader;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCsvRecord {
    private String isbn13;
    private String isbn10;
    private String title;
    private String subtitle;
    private String authors;
    private String categories;
    private String thumbnail;
    private String description;
    private String publishedYear;
    private String averageRating;
    private String numPages;
    private String ratingsCount;
}