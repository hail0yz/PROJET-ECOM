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


    /**
     * Create a Specification to filter books based on various criteria
     *
     * @param filter A BookFilter
     * @return A Specification of Book representing the combined filtering conditions
     */
    public static Specification<Book> filter(BookFilter filter) {
        Assert.notNull(filter, () -> "Book Filter must not be null");

        return Specification.allOf(
                search(filter.search()),
                priceGreaterThanOrEqualTo(filter.minPrice()),
                priceLessThanOrEqualTo(filter.maxPrice()),
                orderBy(filter.sortBy(), filter.direction()),
                booksByCategoryId(filter.categoryId())
        );
    }


    /**
     * Create a Specification to filter books based on a keyword and search in a book's title, author or summary. This keyword id case-insensitive.
     *
     * @param search A keyword
     * @return A Specification of Book to filter books by a keyword
     */
    public static Specification<Book> search(String search) {
        if (search == null || search.isBlank()) {
            return Specification.unrestricted();
        }

        String lowerSearch = "%" + search.toLowerCase() + "%";

        return (root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("summary")), lowerSearch),
                        cb.like(cb.lower(root.get("author")), lowerSearch),
                        cb.like(cb.lower(root.get("title")), lowerSearch)
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


    /**
     *
     * @param sortBy
     * @param direction
     * @return
     */
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


    /**
     * Create a Specification to filter books whose category id matches the one given in parameter
     *
     * @param categoryId The id of a category
     * @return A Specification of Book to filter books by the id category
     */
    public static Specification<Book> booksByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return Specification.unrestricted();
        }

        return (root, query, cb) ->
                cb.equal(root.get("category").get("categoryId"), categoryId);
    }

}
