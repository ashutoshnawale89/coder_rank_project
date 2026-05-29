package com.code.rank.service;

import com.code.rank.dto.response.SubmissionResponse;
import com.code.rank.entity.Submission;
import com.code.rank.exception.ForbiddenException;
import com.code.rank.exception.ResourceNotFoundException;
import com.code.rank.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public Page<SubmissionResponse> list(Long userId, Pageable pageable) {
        return submissionRepository.findByUserId(userId, pageable).map(SubmissionResponse::from);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse get(Long userId, Long id) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        if (!s.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this submission");
        }
        return SubmissionResponse.from(s);
    }
}
