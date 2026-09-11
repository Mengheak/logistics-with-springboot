package com.chheang.mengheak.logisticsapis.exception;

import org.springframework.http.HttpStatus;

/** 404 - the addressed resource does not exist. */
public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public static NotFoundException of(String resource, Object identifier) {
        return new NotFoundException("%s not found for identifier = %s".formatted(resource, identifier));
    }
}
