package com.chheang.mengheak.logisticsapis.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Base type for every exception the API translates into a structured error body.
 * Subclasses fix the {@link HttpStatus} so call sites only describe what went wrong.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, String> errors;

    protected ApiException(HttpStatus status, String message) {
        this(status, message, null);
    }

    protected ApiException(HttpStatus status, String message, Map<String, String> errors) {
        super(message);
        this.status = status;
        this.errors = errors;
    }

    public String getCode() {
        return String.valueOf(status.value());
    }
}
