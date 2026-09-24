package com.mutuals.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    protected ConflictException(String message, Map<String, Object> details) {
        super(HttpStatus.CONFLICT, message, details);
    }
}
