package roomescape.application;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.exception.dto.ExceptionResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ExceptionResponse.createByMessage(exception.getMessage()));
    }

    @ExceptionHandler({EmptyResultDataAccessException.class, BadRequestException.class})
    public ResponseEntity<ExceptionResponse> handleBadRequestException(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.createByMessage(exception.getMessage()));
    }
}