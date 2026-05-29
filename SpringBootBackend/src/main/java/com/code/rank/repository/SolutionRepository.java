package com.code.rank.repository;

import com.code.rank.entity.Solution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolutionRepository extends JpaRepository<Solution, Long> {
    Page<Solution> findByUserId(Long userId, Pageable pageable);
    Page<Solution> findByUserIdAndQuestionId(Long userId, Long questionId, Pageable pageable);
}
