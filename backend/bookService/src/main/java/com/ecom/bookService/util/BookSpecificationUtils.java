package com.ecom.bookService.util;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import com.ecom.bookService.dto.BookFilter;
import com.ecom.bookService.model.Book;

public class BookSpecificationUtils {

    private BookSpecificationUtils() {
    }

    public static Specification<Book> filter(BookFilter filter) {
        Assert.notNull(filter, () -> "Book Filter must not be null");

        return Specification.allOf(
                search(filter.search()),
                priceGreaterThanOrEqualTo(filter.minPrice()),
                priceLessThanOrEqualTo(filter.maxPrice()),
                orderBy(filter.sortBy(), filter.direction())
        );
    }

    public static Specification<Book> search(String search) {
        if (search == null || search.isBlank()) {
            return Specification.unrestricted();
        }

        return (root, query, cb) ->
                cb.or(
                        cb.like(root.get("summary"), "%" + search + "%"),
                        cb.like(root.get("author"), "%" + search + "%"),
                        cb.like(root.get("title"), "%" + search + "%")
                );
    }

    /**
     * Creates a Specification to filter Books by author (case-insensitive, contains).
     *
     * @param author The author fragment to search for.
     * @return A Specification for the author filter.
     */
    public static Specification<Book> hasAuthorContaining(String author) {
        if (author == null || author.isBlank()) {
            return Specification.unrestricted();
        }

        String lowerCaseAuthor = "%" + author.toLowerCase() + "%";

        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("author")),
                        lowerCaseAuthor
                );
    }

    /**
     * Creates a Specification to filter Books with a price greater than or equal to minPrice.
     *
     * @param minPrice The minimum price.
     * @return A {@code Specification} for the minimum price filter.
     */
    public static Specification<Book> priceGreaterThanOrEqualTo(BigDecimal minPrice) {
        if (minPrice == null) {
            return Specification.unrestricted();
        }
        return (root, query, db) ->
                db.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    /**
     * Creates a Specification to filter Books with a price less than or equal to maxPrice.
     *
     * @param maxPrice The maximum price.
     * @return A {@code Specification} for the maximum price filter.
     */
    public static Specification<Book> priceLessThanOrEqualTo(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return Specification.unrestricted();
        }
        return (root, query, db) ->
                db.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Book> orderBy(BookFilter.BookSortBy sortBy, Sort.Direction direction) {
        BookFilter.BookSortBy sort = Objects.requireNonNullElse(sortBy, BookFilter.BookSortBy.PUBLISH_DATE);

        return (root, query, cb) -> {
            var path = root.get(sort.attribute());

            if (Sort.Direction.ASC == direction) {
                cb.asc(path);
            }
            else {
                cb.desc(path);
            }

            return cb.conjunction();
        };
    }

}
