package com.chheang.mengheak.logisticsapis.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/** 400 - the request itself is malformed or self-contradicting. */
public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public BadRequestException(String message, Map<String, String> errors) {
        super(HttpStatus.BAD_REQUEST, message, errors);
    }
}
