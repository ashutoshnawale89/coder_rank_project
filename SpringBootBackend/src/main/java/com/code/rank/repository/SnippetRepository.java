package com.code.rank.repository;

import com.code.rank.entity.Snippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnippetRepository extends JpaRepository<Snippet, Long> {
    Page<Snippet> findByUserId(Long userId, Pageable pageable);
}
