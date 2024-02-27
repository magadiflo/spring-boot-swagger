package dev.magadiflo.swagger.app.web.advice;

import dev.magadiflo.swagger.app.exception.ApiException;
import dev.magadiflo.swagger.app.web.util.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiAdvice {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResponseMessage<Void>> apiException(ApiException e) {
        ResponseMessage<Void> response = new ResponseMessage<>(e.getMessage(), null);
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }
}
