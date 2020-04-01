package com.github.al.realworld.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException notFound(String message, Object... args) {
        return new ResourceNotFoundException(String.format(message, args));
    }
}