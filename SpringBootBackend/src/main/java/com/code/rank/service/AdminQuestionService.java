package com.code.rank.service;

import com.code.rank.dto.request.QuestionRequest;
import com.code.rank.dto.request.TestCaseRequest;
import com.code.rank.dto.response.QuestionResponse;
import com.code.rank.entity.Question;
import com.code.rank.entity.TestCase;
import com.code.rank.entity.User;
import com.code.rank.exception.BadRequestException;
import com.code.rank.exception.ResourceNotFoundException;
import com.code.rank.repository.QuestionRepository;
import com.code.rank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminQuestionService {

    static final int MAX_SAMPLE = 5;
    static final int MAX_HIDDEN = 100;

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<QuestionResponse> create(Long adminId, List<QuestionRequest> requestList) {
         User admin = userRepository.findById(adminId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
        List<QuestionResponse> responses = new java.util.ArrayList<>();
        for (QuestionRequest req : requestList) {
            validateTestCases(req.getTestCases());
            Question question = Question.builder()
                    .title(req.getTitle())
                    .description(req.getDescription())
                    .difficulty(req.getDifficulty())
                    .createdBy(admin)
                    .build();

            List<TestCase> testCases = buildTestCases(question, req.getTestCases());
            question.setTestCases(testCases);
            responses.add(QuestionResponse.forAdmin(questionRepository.save(question)));
        }
        return responses;
    }

    @Transactional
    public QuestionResponse update(Long id, QuestionRequest req) {
        validateTestCases(req.getTestCases());
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        question.setTitle(req.getTitle());
        question.setDescription(req.getDescription());
        question.setDifficulty(req.getDifficulty());

        question.getTestCases().clear();
        question.getTestCases().addAll(buildTestCases(question, req.getTestCases()));
        return QuestionResponse.forAdmin(questionRepository.save(question));
    }

    @Transactional(readOnly = true)
    public QuestionResponse get(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        return QuestionResponse.forAdmin(q);
    }

    @Transactional
    public void delete(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        questionRepository.delete(q);
    }

    private void validateTestCases(List<TestCaseRequest> testCases) {
        long sample = testCases.stream().filter(t -> Boolean.TRUE.equals(t.getSample())).count();
        long hidden = testCases.size() - sample;
        if (sample > MAX_SAMPLE) {
            throw new BadRequestException("Too many sample test cases (max " + MAX_SAMPLE + ")");
        }
        if (hidden > MAX_HIDDEN) {
            throw new BadRequestException("Too many hidden test cases (max " + MAX_HIDDEN + ")");
        }
        if (testCases.stream().anyMatch(t -> t.getExpectedOutput() == null)) {
            throw new BadRequestException("Each test case must have an expected output");
        }
    }

    private List<TestCase> buildTestCases(Question question, List<TestCaseRequest> requests) {
        return java.util.stream.IntStream.range(0, requests.size())
                .mapToObj(i -> {
                    TestCaseRequest r = requests.get(i);
                    return TestCase.builder()
                            .question(question)
                            .input(r.getInput() == null ? "" : r.getInput())
                            .expectedOutput(r.getExpectedOutput())
                            .sample(Boolean.TRUE.equals(r.getSample()))
                            .orderIndex(i)
                            .build();
                })
                .toList();
    }
}
