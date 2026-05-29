package com.code.rank.repository;

import com.code.rank.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByQuestionIdOrderByOrderIndexAsc(Long questionId);
    long countByQuestionIdAndSample(Long questionId, boolean sample);
}
