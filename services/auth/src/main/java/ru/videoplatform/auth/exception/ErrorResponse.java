package ru.videoplatform.auth.exception;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {
    int status;
    String error;
    String message;
    Instant createdAt;

    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                Instant.now()
        );
    }
}
