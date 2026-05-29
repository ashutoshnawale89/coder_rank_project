package com.code.rank.exception;

public class ExecutionFailedException extends RuntimeException {
    public ExecutionFailedException(String message) {
        super(message);
    }

    public ExecutionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
