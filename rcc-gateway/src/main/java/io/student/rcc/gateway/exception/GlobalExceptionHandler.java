package io.student.rcc.gateway.exception;

import io.grpc.StatusRuntimeException;
import io.student.rcc.gateway.model.ErrorJson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ErrorJson> handleGrpc(StatusRuntimeException ex) {
        HttpStatus status = switch (ex.getStatus().getCode()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case PERMISSION_DENIED, UNAUTHENTICATED -> HttpStatus.FORBIDDEN;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        String msg = ex.getStatus().getDescription() == null
                ? status.getReasonPhrase()
                : ex.getStatus().getDescription();
        return ResponseEntity.status(status).body(ErrorJson.ofMessage(msg));
    }
}