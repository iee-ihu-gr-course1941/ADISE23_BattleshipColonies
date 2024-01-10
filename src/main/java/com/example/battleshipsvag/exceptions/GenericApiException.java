package com.example.battleshipsvag.exceptions;

public class GenericApiException extends RuntimeException {
    int statusCode;
    public GenericApiException(String message) {
        super(message);
    }

    public GenericApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
