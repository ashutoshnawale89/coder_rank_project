package com.code.rank.service;

import com.code.rank.dto.response.QuestionResponse;
import com.code.rank.dto.response.QuestionSummaryResponse;
import com.code.rank.entity.Question;
import com.code.rank.exception.ResourceNotFoundException;
import com.code.rank.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public Page<QuestionSummaryResponse> list(Pageable pageable) {
        return questionRepository.findAll(pageable).map(QuestionSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public QuestionResponse get(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        return QuestionResponse.forUser(q);
    }
}
