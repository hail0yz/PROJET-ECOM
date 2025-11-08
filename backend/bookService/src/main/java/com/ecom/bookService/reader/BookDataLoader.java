package com.ecom.bookService.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.Category;
import com.ecom.bookService.repository.BookRepository;
import com.ecom.bookService.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookDataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final BookCsvParser csvParser;
    private final BookDataValidator bookCsvValidator;

    public void run(String... args) {
        if (bookRepository.count() > 0) {
            log.info("Database already contains data. Skipping CSV import.");
            return;
        }

        var resource = new ClassPathResource("books.csv");
        try {
            List<BookCsvRecord> records = csvParser.parseCsv(resource.getInputStream());
            log.info("Parsed {} records from CSV", records.size());

            PopulateResult result = processRecords(records);
            log.info("Data population result: {}", result);
        }
        catch (Exception e) {
            log.error("Failed to load csv file", e);
        }
    }

    private PopulateResult processRecords(List<BookCsvRecord> csvRecords) {
        List<Book> validBooks = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();
        List<String> persistenceErrors = new ArrayList<>();
        int duplicates = 0;

        for (int i = 0; i < csvRecords.size(); i++) {
            BookCsvRecord record = csvRecords.get(i);
            BookDataValidator.ValidationResult validation = bookCsvValidator.validate(record);

            if (!validation.isValid()) {
                String error = String.format("Record %d (ISBN13: %s) validation failed: %s",
                        i + 1, record.getIsbn13(), String.join(", ", validation.getErrors()));
                validationErrors.add(error);
                continue;
            }

            try {
                Book book = convertToEntity(record);

                if (book == null) continue;

                book.setCategory(getOrCreateCategory(record.getCategories()));

                if (bookRepository.existsByIsbn13(book.getIsbn13())) {
                    duplicates++;
                    log.warn("Duplicate book skipped: {}", book.getIsbn13());
                    continue;
                }

                validBooks.add(book);
            }
            catch (Exception e) {
                String error = String.format("Record %d (ISBN13: %s) conversion failed: %s",
                        i + 1, record.getIsbn13(), e.getMessage());
                persistenceErrors.add(error);
            }
        }

        if (!validBooks.isEmpty()) {
            try {
                List<Book> savedBooks = bookRepository.saveAll(validBooks);
                return new PopulateResult(
                        savedBooks.size(),
                        validationErrors.size(),
                        persistenceErrors.size(),
                        duplicates,
                        validationErrors,
                        persistenceErrors
                );
            }
            catch (Exception e) {
                log.error("Failed to save books to database", e);
                throw new RuntimeException("Database persistence failed", e);
            }
        }

        return new PopulateResult(0, validationErrors.size(), persistenceErrors.size(),
                duplicates, validationErrors, persistenceErrors);
    }

    private Category getOrCreateCategory(String categories) {
        if (categories == null || categories.isBlank()) {
            return null;
        }

        Optional<String> categoryName = Stream.of(categories.split(","))
                .map(String::trim)
                .findFirst();

        return categoryName.map(s -> categoryRepository.findByCategoryName(s)
                        .orElseGet(() -> {
                            var category = Category.builder()
                                    .categoryName(s)
                                    .build();
                            return categoryRepository.save(category);
                        }))
                .orElse(null);
    }

    private Book convertToEntity(BookCsvRecord record) {
        if (record.getAuthors().trim().length() > 100) {
            return null;
        }

        Book book = new Book();

        book.setIsbn13(record.getIsbn13().trim());
        book.setIsbn10(record.getIsbn10() != null ? record.getIsbn10().trim() : null);
        book.setTitle(record.getTitle().trim());
        book.setSubtitle(record.getSubtitle() != null ? record.getSubtitle().trim() : null);
        book.setAuthor(record.getAuthors().trim());
        // TODO book.setCategories(record.getCategories().trim());
        book.setThumbnail(record.getThumbnail());
        book.setSummary(record.getDescription());

        if (record.getPublishedYear() != null) {
            book.setPublishedYear(Integer.parseInt(record.getPublishedYear()));
        }

        if (record.getNumPages() != null) {
            book.setNumPages(Integer.parseInt(record.getNumPages()));
        }

        return book;
    }

    @ToString
    public static class PopulateResult {
        private final int savedCount;
        private final int validationErrorCount;
        private final int persistenceErrorCount;
        private final int duplicateCount;
        private final List<String> validationErrors;
        private final List<String> persistenceErrors;

        public PopulateResult(int savedCount, int validationErrorCount, int persistenceErrorCount,
                              int duplicateCount, List<String> validationErrors, List<String> persistenceErrors) {
            this.savedCount = savedCount;
            this.validationErrorCount = validationErrorCount;
            this.persistenceErrorCount = persistenceErrorCount;
            this.duplicateCount = duplicateCount;
            this.validationErrors = validationErrors;
            this.persistenceErrors = persistenceErrors;
        }
    }

}