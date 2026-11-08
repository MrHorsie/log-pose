package com.logpose.api.controller;

import com.logpose.api.search.SearchQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Converts exceptions into RFC 9457 problem responses, so the frontend always gets a readable message.
 * Extending ResponseEntityExceptionHandler covers Spring's own errors (validation, 404s) too.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(SearchQueryException.class)
    public ProblemDetail handleBadQuery(SearchQueryException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Invalid search query");
        return problem;
    }
}

