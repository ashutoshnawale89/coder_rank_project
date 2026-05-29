package com.code.rank.service;

import com.code.rank.exception.RateLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final long WINDOW_MS = 60_000L;

    private final int maxRequests;
    private final Map<Long, Deque<Long>> hits = new ConcurrentHashMap<>();

    public RateLimiterService(@Value("${app.rate-limit.requests-per-minute}") int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public void check(Long userId) {
        long now = System.currentTimeMillis();
        Deque<Long> deque = hits.computeIfAbsent(userId, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && now - deque.peekFirst() > WINDOW_MS) {
                deque.pollFirst();
            }
            if (deque.size() >= maxRequests) {
                throw new RateLimitException(
                        "Rate limit exceeded: max " + maxRequests + " requests per minute");
            }
            deque.addLast(now);
        }
    }
}
