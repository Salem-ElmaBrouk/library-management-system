package com.lms.backend.service;

import com.lms.backend.entity.AppUser;
import com.lms.backend.entity.Book;
import com.lms.backend.entity.Loan;
import com.lms.backend.entity.Role;
import com.lms.backend.exception.DomainExceptions.AccessDeniedException;
import com.lms.backend.exception.DomainExceptions.BusinessRuleException;
import com.lms.backend.exception.DomainExceptions.ResourceNotFoundException;
import com.lms.backend.repository.AppUserRepository;
import com.lms.backend.repository.BookRepository;
import com.lms.backend.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final AppUserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public Loan checkoutBook(Long bookId, String userEmail) {
        // 1. Lock the book row (Pessimistic Lock)
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException("No available copies for this book.");
        }

        AppUser user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Decrement stock
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // 3. Create loan
        Loan loan = Loan.builder()
                .book(book)
                .user(user)
                .borrowedAt(Instant.now())
                .dueDate(Instant.now().plus(14, ChronoUnit.DAYS))
                .build();

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(Long loanId, String userEmail, Role userRole) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan not found"));

        // Authorization check
        if (!loan.getUser().getEmail().equalsIgnoreCase(userEmail)
                && userRole != Role.LIBRARIAN) {

            throw new AccessDeniedException(
                    "You can only return your own books."
            );
        }

        // Prevent duplicate returns
        if (loan.getReturnedAt() != null) {
            throw new BusinessRuleException(
                    "This loan has already been returned."
            );
        }

        loan.setReturnedAt(Instant.now());

        return loanRepository.save(loan);
    }

    @Transactional(readOnly = true)
    public List<Loan> getMyLoans(String email) {
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return loanRepository.findByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }
}