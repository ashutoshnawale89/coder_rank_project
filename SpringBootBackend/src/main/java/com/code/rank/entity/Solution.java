package com.code.rank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "solutions", indexes = {
        @Index(name = "idx_solution_user", columnList = "user_id"),
        @Index(name = "idx_solution_question", columnList = "question_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Language language;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String code;

    @Column(nullable = false)
    private int passedCount;

    @Column(nullable = false)
    private int totalCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SolutionStatus status;

    @Column(nullable = false)
    private long totalExecutionTimeMs;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
