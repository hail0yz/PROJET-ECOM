package com.ecom.bookService.reader;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;


@Service
public class BookDataValidator {
    
    private static final Set<String> VALID_IMAGE_EXTENSIONS =
        Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Pattern ISBN13_PATTERN = Pattern.compile("^\\d{13}$");
    private static final Pattern ISBN10_PATTERN = Pattern.compile("^\\d{9}[\\dX]$");
    private static final int CURRENT_YEAR = Year.now().getValue();

    public ValidationResult validate(BookCsvRecord record) {
        List<String> errors = new ArrayList<>();

        if (isBlank(record.getIsbn13())) {
            errors.add("ISBN13 is required");
        } else if (!isValidIsbn13(record.getIsbn13())) {
            errors.add("Invalid ISBN13 format: " + record.getIsbn13());
        }

        if (isBlank(record.getTitle())) {
            errors.add("Title is required");
        }

        if (isBlank(record.getAuthors())) {
            errors.add("Authors are required");
        }

        if (isBlank(record.getCategories())) {
            errors.add("Categories are required");
        }

        if (!isBlank(record.getIsbn10()) && !isValidIsbn10(record.getIsbn10())) {
            errors.add("Invalid ISBN10 format: " + record.getIsbn10());
        }

        if (!isBlank(record.getPublishedYear())) {
            validateYear(record.getPublishedYear(), errors);
        }

        if (!isBlank(record.getAverageRating())) {
            validateRating(record.getAverageRating(), errors);
        }

        if (!isBlank(record.getNumPages())) {
            validateNumPages(record.getNumPages(), errors);
        }

        if (!isBlank(record.getThumbnail())) {
            validateThumbnailUrl(record.getThumbnail(), errors);
        }

        return new ValidationResult(errors.isEmpty(), errors, record);
    }

    private boolean isValidIsbn13(String isbn13) {
        return ISBN13_PATTERN.matcher(isbn13).matches();
    }

    private boolean isValidIsbn10(String isbn10) {
        return ISBN10_PATTERN.matcher(isbn10).matches();
    }

    private void validateYear(String yearStr, List<String> errors) {
        try {
            int year = Integer.parseInt(yearStr);
            if (year < 1000 || year > CURRENT_YEAR + 5) { // Allow future books up to 5 years
                errors.add("Invalid publication year: " + year);
            }
        } catch (NumberFormatException e) {
            errors.add("Invalid year format: " + yearStr);
        }
    }

    private void validateRating(String ratingStr, List<String> errors) {
        try {
            double rating = Double.parseDouble(ratingStr);
            if (rating < 0 || rating > 5) {
                errors.add("Average rating must be between 0 and 5: " + rating);
            }
        } catch (NumberFormatException e) {
            errors.add("Invalid rating format: " + ratingStr);
        }
    }

    private void validateNumPages(String pagesStr, List<String> errors) {
        try {
            int pages = Integer.parseInt(pagesStr);
            if (pages <= 0 || pages > 50000) {
                errors.add("Number of pages must be positive and reasonable: " + pages);
            }
        } catch (NumberFormatException e) {
            errors.add("Invalid number of pages format: " + pagesStr);
        }
    }

    private void validateThumbnailUrl(String thumbnail, List<String> errors) {
        try {
            new URL(thumbnail); // Validate URL format
        } catch (MalformedURLException e) {
            errors.add("Invalid thumbnail URL: " + thumbnail);
        }
    }

    private String getFileExtension(String url) {
        int lastDot = url.lastIndexOf('.');
        return lastDot > 0 ? url.substring(lastDot + 1) : "";
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final BookCsvRecord record;

        public ValidationResult(boolean valid, List<String> errors, BookCsvRecord record) {
            this.valid = valid;
            this.errors = errors;
            this.record = record;
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public BookCsvRecord getRecord() { return record; }
    }

}