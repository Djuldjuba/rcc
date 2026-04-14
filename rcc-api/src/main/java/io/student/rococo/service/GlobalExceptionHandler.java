package io.student.rococo.service;

import io.student.rococo.exception.ResourceNotFoundException;
import io.student.rococo.model.ErrorJson;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorJson> handleValidationException(MethodArgumentNotValidException ex) {
    List<String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.toList());

    LOG.warn("Validation error: {}", errors);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorJson.of(errors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorJson> handleConstraintViolationException(ConstraintViolationException ex) {
    List<String> errors = ex.getConstraintViolations()
        .stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toList());

    LOG.warn("Constraint violation: {}", errors);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorJson.of(errors));
  }

  @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class, ResourceNotFoundException.class})
  public ResponseEntity<ErrorJson> handleNotFoundException(RuntimeException ex) {
    LOG.warn("Entity not found: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorJson.ofMessage(ex.getMessage() != null
            ? ex.getMessage()
            : "Запрашиваемый ресурс не найден"
        ));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorJson> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
    String error = String.format("Параметр '%s' имеет неверный формат", ex.getName());
    LOG.warn("### Type mismatch: {}", error);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorJson.of(error));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorJson> handleIllegalArgumentException(IllegalArgumentException ex) {
    LOG.warn("### Illegal argument: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorJson.ofMessage(ex.getMessage() != null
                ? ex.getMessage() :
                "Некорректные данные запроса"
            ));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorJson> handleIllegalStateException(IllegalStateException ex) {
    LOG.warn("### Illegal state: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(
            ErrorJson.ofMessage(ex.getMessage() != null
                ? ex.getMessage()
                : "Операция не может быть выполнена в текущем состоянии"
            ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorJson> handleGenericException(Exception ex) {
    LOG.error("### Unexpected error occurred", ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ErrorJson.ofMessage("Произошла внутренняя ошибка сервера")
        );
  }
}
