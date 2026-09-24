package com.mutuals.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final transient Map<String, Object> details;

    protected ApiException(HttpStatus status, String message) {
        this(status, message, null);
    }

    protected ApiException(HttpStatus status, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.details = details;
    }
}
