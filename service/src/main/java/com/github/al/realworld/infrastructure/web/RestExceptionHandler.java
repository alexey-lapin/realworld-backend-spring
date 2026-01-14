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
import com.github.al.realworld.application.exception.ForbiddenException;
import com.github.al.realworld.application.exception.NotFoundException;
import com.github.al.realworld.application.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericError> handleException(Exception e) {
        var message = List.of(e.getMessage());
        if (e instanceof ApplicationException ex) {
            return switch (ex) {
                case BadRequestException _ -> responseEntity(HttpStatus.UNPROCESSABLE_CONTENT, message);
                case ForbiddenException _ -> responseEntity(HttpStatus.FORBIDDEN, message);
                case NotFoundException _ -> responseEntity(HttpStatus.NOT_FOUND, message);
                case UnauthorizedException _ -> responseEntity(HttpStatus.UNAUTHORIZED, message);
            };
        } else if (e instanceof ErrorResponse errorResponse) {
            if (errorResponse instanceof MethodArgumentNotValidException ex) {
                return handleMethodArgumentNotValid(ex);
            } else if (errorResponse instanceof BindException ex) {
                return handleBindException(ex);
            } else {
                var statusCode = errorResponse.getStatusCode();
                if (statusCode.isSameCodeAs(HttpStatus.UNAUTHORIZED)
                    || statusCode.isSameCodeAs(HttpStatus.FORBIDDEN)) {
                    responseEntity(statusCode, message);
                } else if (statusCode.is4xxClientError()) {
                    responseEntity(HttpStatus.UNPROCESSABLE_CONTENT, message);
                } else {
                    responseEntity(statusCode, message);
                }
            }
        } else if (e instanceof ConstraintViolationException ex) {
            return handleConstraintViolation(ex);
        }
        return responseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message);
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
        var errors = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        return responseEntity(HttpStatus.UNPROCESSABLE_CONTENT, errors);
    }

    private static ResponseEntity<GenericError> responseEntityObjectErrors(
            HttpStatusCode httpStatusCode, List<ObjectError> errors) {
        var messages = errors.stream()
                .map(ObjectError::getDefaultMessage)
                .toList();
        return responseEntity(httpStatusCode, messages);
    }

    private static ResponseEntity<GenericError> responseEntity(
            HttpStatusCode httpStatusCode, List<String> errors) {
        var body = new GenericError(new GenericError.Errors(errors));
        return ResponseEntity.status(httpStatusCode).body(body);
    }

}
