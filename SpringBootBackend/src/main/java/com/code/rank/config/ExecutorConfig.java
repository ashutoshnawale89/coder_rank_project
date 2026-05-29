package com.code.rank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfig {

    @Bean(name = "executionExecutor", destroyMethod = "shutdown")
    public ThreadPoolExecutor executionExecutor(
            @Value("${app.executor.core-pool-size}") int core,
            @Value("${app.executor.max-pool-size}") int max,
            @Value("${app.executor.queue-capacity}") int queue) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                core, max, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queue),
                new ThreadPoolExecutor.AbortPolicy());
        executor.allowCoreThreadTimeOut(false);
        return executor;
    }
}
