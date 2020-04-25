package com.github.al.realworld.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public static UnauthorizedException unauthorized(String message, Object... args) {
        return new UnauthorizedException(String.format(message, args));
    }

}
