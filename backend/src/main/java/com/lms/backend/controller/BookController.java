package com.lms.backend.controller;

import com.lms.backend.dto.BookRequest;
import com.lms.backend.dto.BookResponse;
import com.lms.backend.entity.Book;
import com.lms.backend.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // 1. Search books
    @GetMapping
    public ResponseEntity<List<BookResponse>> searchBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean available
    ) {
        List<Book> books = bookService.searchBooks(q, category, available, null, null);
        List<BookResponse> response = books.stream().map(BookResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    // 2. Get single book
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBook(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(BookResponse.from(book));
    }

    // 3. Create book
    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        // Map Request DTO to Entity
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .category(request.category())
                .availableCopies(request.availableCopies())
                .build();

        Book savedBook = bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.from(savedBook));
    }

    // 4. Update book
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        Book bookDetails = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .category(request.category())
                .availableCopies(request.availableCopies())
                .build();

        Book updatedBook = bookService.updateBook(id, bookDetails);
        return ResponseEntity.ok(BookResponse.from(updatedBook));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}