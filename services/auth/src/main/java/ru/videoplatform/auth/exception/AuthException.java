package ru.videoplatform.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthException extends ResponseStatusException {

    public AuthException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public static AuthException conflict(String message) {
        return new AuthException(HttpStatus.CONFLICT, message);
    }

    public static AuthException badRequest(String message) {
        return new AuthException(HttpStatus.BAD_REQUEST, message);
    }

    public static AuthException unauthorized(String message) {
        return new AuthException(HttpStatus.UNAUTHORIZED, message);
    }
}