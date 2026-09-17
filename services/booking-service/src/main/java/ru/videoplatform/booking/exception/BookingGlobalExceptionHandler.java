package ru.videoplatform.booking.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.videoplatform.booking.dto.ErrorResponseDto;

@RestControllerAdvice
public class BookingGlobalExceptionHandler {

    @ExceptionHandler(SlotConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleBusinessConflict(SlotConflictException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto("Conflict", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDatabaseConflict(DataIntegrityViolationException ex) {
        String rootMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";

        if (rootMessage != null && rootMessage.contains("unique_index_start_time")) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponseDto(
                            "Conflict",
                            "Данное время уже забронировано.")
                    );
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(
                        "Bad Request",
                        "Не удалось сохранить запись. Попробуйте позже.")
                );
    }
}
