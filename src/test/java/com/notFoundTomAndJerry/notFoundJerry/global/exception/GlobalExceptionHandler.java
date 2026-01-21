package com.notFoundTomAndJerry.notFoundJerry.global.exception;

import com.notFoundTomAndJerry.notFoundJerry.global.exception.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
    ApiCode code = e.getCode();
    return ResponseEntity
        .status(code.getHttpStatus())
        .body(new ErrorResponse(code.getCode(), e.resolvedMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAny(Exception e) {
    ApiCode code = CommonErrorCode.SERVER_ERROR;
    return ResponseEntity
        .status(code.getHttpStatus())
        .body(new ErrorResponse(code.getCode(), code.getMessage()));
  }
}
