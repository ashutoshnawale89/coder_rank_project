package com.code.rank.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_cases", indexes = @Index(name = "idx_tc_question", columnList = "question_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String input;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String expectedOutput;

    @Column(nullable = false)
    private boolean sample;

    @Column(nullable = false)
    private int orderIndex;
}
