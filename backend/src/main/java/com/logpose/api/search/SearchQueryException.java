package com.logpose.api.search;

/**
 * Thrown for queries that can't be parsed or that use a field incorrectly. Reported to the client as HTTP 400.
 */
public class SearchQueryException extends RuntimeException {

    public SearchQueryException(String message) {
        super(message);
    }
}
