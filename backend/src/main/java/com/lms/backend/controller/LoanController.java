package com.lms.backend.controller;

import com.lms.backend.entity.Loan;
import com.lms.backend.security.AuthUser;
import com.lms.backend.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    // Checkout a book
    @PostMapping("/checkout/{bookId}")
    public ResponseEntity<Loan> checkout(@PathVariable Long bookId, @AuthenticationPrincipal AuthUser authUser) {
        Loan loan = loanService.checkoutBook(bookId, authUser.getUsername());
        return ResponseEntity.ok(loan);
    }

    // Return a book
    @PostMapping("/return/{loanId}")
    public ResponseEntity<Loan> returnBook(@PathVariable Long loanId, @AuthenticationPrincipal AuthUser authUser) {
        Loan loan = loanService.returnBook(loanId, authUser.getUsername(), authUser.user().getRole());
        return ResponseEntity.ok(loan);
    }

    // Get my loans
    @GetMapping("/my-loans")
    public ResponseEntity<List<Loan>> getMyLoans(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(loanService.getMyLoans(authUser.getUsername()));
    }

    // Get all loans (Staff only)
    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }
}