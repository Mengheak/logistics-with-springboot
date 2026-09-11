package com.chheang.mengheak.logisticsapis.exception;

import org.springframework.http.HttpStatus;

/** 403 - the caller is authenticated but not allowed to perform this action. */
public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
