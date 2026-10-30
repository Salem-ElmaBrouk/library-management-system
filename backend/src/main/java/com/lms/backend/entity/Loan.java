package com.lms.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "loan") // Check your SQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ALWAYS LAZY! Eager loading causes the N+1 query problem.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id",nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;


    @Column(nullable = false)
    private Instant borrowedAt;

    @Column(nullable = false)
    private Instant dueDate;

    private Instant returnedAt;


    @Column(name = "due_soon_notified_at")
    private Instant dueSoonNotifiedAt;

    @Column(name = "overdue_notified_at")
    private Instant overdueNotifiedAt;


}
