package com.chheang.mengheak.logisticsapis.exception;

import org.springframework.http.HttpStatus;

/**
 * 422 - the request is syntactically valid but violates a domain rule: an illegal status
 * transition, an over-capacity trip, dispatching an empty manifest, and so on.
 */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    public static BusinessRuleException illegalTransition(String resource, Object from, Object to) {
        return new BusinessRuleException(
                "%s cannot move from %s to %s".formatted(resource, from, to));
    }
}
