package com.lms.backend.service;

 

import com.lms.backend.entity.Book;
import com.lms.backend.exception.DomainExceptions.ResourceNotFoundException;
import com.lms.backend.repository.BookRepository;
import com.lms.backend.repository.BookSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public List<Book> searchBooks(String q, String category, Boolean available, Integer yearFrom, Integer yearTo) {
        return bookRepository.findAll(BookSpecs.search(q, category, available, yearFrom, yearTo));
    }

    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    @Transactional
    public Book createBook(Book book) {
        book.setCreatedAt(Instant.now());
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, Book updatedBook) {
        Book existing = getBookById(id);
        existing.setTitle(updatedBook.getTitle());
        existing.setAuthor(updatedBook.getAuthor());
        existing.setIsbn(updatedBook.getIsbn());
        existing.setCategory(updatedBook.getCategory());
        existing.setAvailableCopies(updatedBook.getAvailableCopies());
        return bookRepository.save(existing);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
    }
}