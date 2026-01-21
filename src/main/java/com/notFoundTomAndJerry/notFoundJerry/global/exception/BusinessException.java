package com.notFoundTomAndJerry.notFoundJerry.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

  private final ApiCode code;
  private final String detailMessage; // optional

  public BusinessException(ApiCode code) {
    super(code.getMessage());
    this.code = code;
    this.detailMessage = null;
  }

  public BusinessException(ApiCode code, String detailMessage) {
    super(code.getMessage());
    this.code = code;
    this.detailMessage = detailMessage;
  }

  public String resolvedMessage() {
    return (detailMessage == null || detailMessage.isBlank())
        ? code.getMessage()
        : detailMessage;
  }
}
