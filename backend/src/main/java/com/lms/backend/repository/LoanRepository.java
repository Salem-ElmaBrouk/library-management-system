package com.lms.backend.repository;

import com.lms.backend.entity.Loan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Override
    @EntityGraph(attributePaths = {"book", "user"})
    List<Loan> findAll();

    @EntityGraph(attributePaths = {"book", "user"})
    List<Loan> findByUserId(Long userId);

    @Query("SELECT l FROM Loan l JOIN FETCH l.book JOIN FETCH l.user WHERE l.returnedAt IS NULL AND l.dueDate BETWEEN :now AND :threshold AND l.dueSoonNotifiedAt IS NULL")
    List<Loan> findDueSoonLoans(Instant now, Instant threshold);

    @Query("SELECT l FROM Loan l JOIN FETCH l.book JOIN FETCH l.user WHERE l.returnedAt IS NULL AND l.dueDate < :now AND l.overdueNotifiedAt IS NULL")
    List<Loan> findOverdueLoans(Instant now);
}