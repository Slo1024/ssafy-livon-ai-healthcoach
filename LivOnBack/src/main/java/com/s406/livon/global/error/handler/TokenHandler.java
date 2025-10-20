package com.s406.livon.global.error.handler;


import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.web.response.code.BaseErrorCode;

public class TokenHandler extends GeneralException {
  public TokenHandler(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
