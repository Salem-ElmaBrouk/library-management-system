package com.lms.backend.dto;


import com.lms.backend.entity.Book;
import java.time.Instant;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        String category,
        Integer availableCopies,
        Instant createdAt
) {
    // Static factory method to convert Entity to DTO
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCategory(),
                book.getAvailableCopies(),
                book.getCreatedAt()
        );
    }
}