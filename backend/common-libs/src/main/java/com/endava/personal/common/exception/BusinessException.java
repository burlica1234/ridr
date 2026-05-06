package com.endava.personal.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus statusCode;
    private final String reason;

    protected BusinessException(HttpStatus statusCode,String message, String reason) {
        super(message);
        this.statusCode = statusCode;
        this.reason = reason;
    }
}
