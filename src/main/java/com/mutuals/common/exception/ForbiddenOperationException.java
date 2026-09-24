package com.mutuals.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ForbiddenOperationException extends ApiException {

    public ForbiddenOperationException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

    protected ForbiddenOperationException(String message, Map<String, Object> details) {
        super(HttpStatus.FORBIDDEN, message, details);
    }
}
