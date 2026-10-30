package com.lms.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "book") // Check your SQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 100, nullable = false)
    private String author;

    @Column(length = 100, nullable = false)
    private String isbn;

    @Column(length = 100, nullable = false)
    private String category;

    @Column( nullable = false)
    private Integer availableCopies;

    @Column(nullable = false)
    private Instant createdAt;







}
