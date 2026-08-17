/*
 * MIT License
 *
 * Copyright (c) 2020 - present Alexey Lapin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.al.realworld.infrastructure.web;

import com.github.al.realworld.api.dto.GenericError;
import com.github.al.realworld.application.exception.ApplicationException;
import com.github.al.realworld.application.exception.BadRequestException;
import com.github.al.realworld.application.exception.ConflictException;
import com.github.al.realworld.application.exception.ForbiddenException;
import com.github.al.realworld.application.exception.NotFoundException;
import com.github.al.realworld.application.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNullElse;

/**
 * Renders exceptions as a {@link GenericError} response, keyed by the field at fault.
 */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    private static final String FALLBACK_FIELD = "body";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericError> handleException(Exception e) {
        log.error("Exception occurred", e);
        if (e instanceof ApplicationException ex) {
            var status = switch (ex) {
                case BadRequestException _ -> HttpStatus.UNPROCESSABLE_CONTENT;
                case ConflictException _ -> HttpStatus.CONFLICT;
                case ForbiddenException _ -> HttpStatus.FORBIDDEN;
                case NotFoundException _ -> HttpStatus.NOT_FOUND;
                case UnauthorizedException _ -> HttpStatus.UNAUTHORIZED;
            };
            return responseEntity(status, GenericError.of(ex.field(), ex.error()));
        } else if (e instanceof ErrorResponse errorResponse) {
            if (errorResponse instanceof MethodArgumentNotValidException ex) {
                return handleMethodArgumentNotValid(ex);
            } else if (errorResponse instanceof BindException ex) {
                return handleBindException(ex);
            } else {
                var statusCode = errorResponse.getStatusCode();
                if (statusCode.isSameCodeAs(HttpStatus.UNAUTHORIZED)
                    || statusCode.isSameCodeAs(HttpStatus.FORBIDDEN)) {
                    return responseEntity(statusCode, fallback(e));
                } else if (statusCode.is4xxClientError()) {
                    return responseEntity(HttpStatus.UNPROCESSABLE_CONTENT, fallback(e));
                } else {
                    return responseEntity(statusCode, fallback(e));
                }
            }
        } else if (e instanceof ConstraintViolationException ex) {
            return handleConstraintViolation(ex);
        }
        return responseEntity(HttpStatus.INTERNAL_SERVER_ERROR, fallback(e));
    }

    public ResponseEntity<GenericError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        return responseEntityObjectErrors(HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getBindingResult().getAllErrors());
    }

    public ResponseEntity<GenericError> handleBindException(BindException exception) {
        return responseEntityObjectErrors(HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getAllErrors());
    }

    public ResponseEntity<GenericError> handleConstraintViolation(
            ConstraintViolationException exception) {
        var pairs = exception.getConstraintViolations().stream()
                .map(violation -> Map.entry(
                        leafField(violation.getPropertyPath().toString()),
                        messageOf(violation)))
                .toList();
        return responseEntity(HttpStatus.UNPROCESSABLE_CONTENT, GenericError.ofPairs(pairs));
    }

    private static String messageOf(ConstraintViolation<?> violation) {
        return requireNonNullElse(violation.getMessage(), "is invalid");
    }

    private static ResponseEntity<GenericError> responseEntityObjectErrors(
            HttpStatusCode httpStatusCode, List<ObjectError> errors) {
        var pairs = errors.stream()
                .map(error -> Map.entry(fieldOf(error), messageOf(error)))
                .toList();
        return responseEntity(httpStatusCode, GenericError.ofPairs(pairs));
    }

    /**
     * Resolves the response key from a binding error, using the leaf segment of the property
     * path.
     */
    private static String fieldOf(ObjectError error) {
        return error instanceof FieldError fieldError
                ? leafField(fieldError.getField())
                : FALLBACK_FIELD;
    }

    private static String leafField(String path) {
        var leaf = path.substring(path.lastIndexOf('.') + 1);
        return leaf.isEmpty() ? FALLBACK_FIELD : leaf;
    }

    private static String messageOf(ObjectError error) {
        return requireNonNullElse(error.getDefaultMessage(), "is invalid");
    }

    private static GenericError fallback(Exception e) {
        return GenericError.of(FALLBACK_FIELD, requireNonNullElse(e.getMessage(), "is invalid"));
    }

    private static ResponseEntity<GenericError> responseEntity(
            HttpStatusCode httpStatusCode, GenericError body) {
        return ResponseEntity.status(httpStatusCode).body(body);
    }

}
