package com.fraus.spring.universityapi.globalexception;

import com.fraus.spring.universityapi.globalexception.customexception.AlreadyExistsException;
import com.fraus.spring.universityapi.globalexception.customexception.InvalidValueException;
import com.fraus.spring.universityapi.globalexception.customexception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception e) {
        log.error("Handle genericException: ", e);

        var errorDto = new ErrorResponseDto(
                "Something went wrong.",
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorDto);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("Handle resourceNotFoundException. Details: {}", e.getLogDetails());

        var errorDto = new ErrorResponseDto(
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleAlreadyExistsException(AlreadyExistsException e) {
        log.error("Handle alreadyExistsException. Details: {}", e.getLogDetails());

        var errorDto = new ErrorResponseDto(
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorDto);
    }

    @ExceptionHandler(InvalidValueException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidValueException(InvalidValueException e) {
        log.error("Handle invalidValueException. Details: {}", e.getLogDetails());

        var errorDto = new ErrorResponseDto(
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Handle methodArgumentNotValidException. Details: {}", e.getMessage());

        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = (fieldError != null)? fieldError.getDefaultMessage(): "Ошибка валидации";
        var errorDto = new ErrorResponseDto(
                message,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentialsException(BadCredentialsException e) {
        log.error("Handle badCredentialsException", e);

        var errorDto = new ErrorResponseDto(
                "Неверная почта или пароль",
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorDto);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidParameterException(InvalidParameterException e) {
        log.error("Handle invalidParameterException. Details: {}", e.getMessage());

        var errorDto = new ErrorResponseDto(
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Handle illegalArgumentException. Details: {}", e.getMessage());

        var errorDto = new ErrorResponseDto(
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDto handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return new ErrorResponseDto(
                "Отказано в доступе",
                LocalDateTime.now()
        );
    }
}
