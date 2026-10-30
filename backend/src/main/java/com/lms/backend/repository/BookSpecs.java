package com.lms.backend.repository;


import com.lms.backend.entity.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecs {

    /**
     * Builds a dynamic WHERE clause for the Book entity.
     * Any parameter passed as null or blank will be ignored in the final SQL query.
     */
    public static Specification<Book> search(String q, String category, Boolean available, Integer yearFrom, Integer yearTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Search by keyword (Title OR Author)
            if (q != null && !q.isBlank()) {
                String likePattern = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), likePattern),
                        cb.like(cb.lower(root.get("author")), likePattern)
                ));
            }

            // 2. Filter by category
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }

            // 3. Filter by availability (true = has copies, false = out of stock)
            if (available != null) {
                if (available) {
                    predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
                } else {
                    predicates.add(cb.equal(root.get("availableCopies"), 0));
                }
            }

            // Note on yearFrom / yearTo:
            // If your Book entity has a 'publishYear' or 'year' column, you would add:
            // if (yearFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("publishYear"), yearFrom));
            // if (yearTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("publishYear"), yearTo));
            // Since we didn't add that column to the Entity/SQL yet, we skip it to prevent crashes.

            // Combine all predicates with AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}