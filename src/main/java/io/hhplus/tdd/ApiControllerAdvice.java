package io.hhplus.tdd;

import io.hhplus.tdd.point.PointException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }

    @ExceptionHandler(PointException.class)
    public ResponseEntity<ErrorResponse> handlePointException(PointException e) {
        return switch (e.getCode()) {
            case "USER_NOT_FOUND" -> ResponseEntity.status(404).body(new ErrorResponse(e.getCode(), e.getMessage()));
            case "NOT_ENOUGH_AMOUNT", "MAX_POINT_LIMIT", "POINT_HISTORY_EMPTY" , "INVALID_AMOUNT" ->
                    ResponseEntity.status(400).body(new ErrorResponse(e.getCode(), e.getMessage()));
            default -> ResponseEntity.status(400).body(new ErrorResponse("POINT_ERROR", e.getMessage()));
        };
    }
}
