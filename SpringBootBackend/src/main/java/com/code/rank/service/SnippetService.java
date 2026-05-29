package com.code.rank.service;

import com.code.rank.dto.request.SnippetRequest;
import com.code.rank.dto.response.SnippetResponse;
import com.code.rank.entity.Snippet;
import com.code.rank.entity.User;
import com.code.rank.exception.ForbiddenException;
import com.code.rank.exception.ResourceNotFoundException;
import com.code.rank.repository.SnippetRepository;
import com.code.rank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SnippetService {

    private final SnippetRepository snippetRepository;
    private final UserRepository userRepository;

    @Transactional
    public SnippetResponse create(Long userId, SnippetRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Snippet snippet = Snippet.builder()
                .user(user)
                .title(req.getTitle())
                .language(req.getLanguage())
                .code(req.getCode())
                .build();
        return SnippetResponse.from(snippetRepository.save(snippet));
    }

    @Transactional(readOnly = true)
    public Page<SnippetResponse> list(Long userId, Pageable pageable) {
        return snippetRepository.findByUserId(userId, pageable).map(SnippetResponse::from);
    }

    @Transactional(readOnly = true)
    public SnippetResponse get(Long userId, Long id) {
        return SnippetResponse.from(loadOwned(userId, id));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Snippet snippet = loadOwned(userId, id);
        snippetRepository.delete(snippet);
    }

    private Snippet loadOwned(Long userId, Long id) {
        Snippet snippet = snippetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet not found"));
        if (!snippet.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this snippet");
        }
        return snippet;
    }
}
