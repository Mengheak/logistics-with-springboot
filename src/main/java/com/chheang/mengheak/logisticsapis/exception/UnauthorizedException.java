package com.chheang.mengheak.logisticsapis.exception;

import org.springframework.http.HttpStatus;

/** 401 - no credentials were supplied, or the ones supplied are invalid or expired. */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
