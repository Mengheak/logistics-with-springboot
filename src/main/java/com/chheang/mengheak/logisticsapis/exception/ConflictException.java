package com.chheang.mengheak.logisticsapis.exception;

import org.springframework.http.HttpStatus;

/** 409 - the request collides with the current state of the resource (duplicates, double booking). */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public static ConflictException duplicate(String resource, String field, Object value) {
        return new ConflictException("%s with %s = %s already exists".formatted(resource, field, value));
    }
}
