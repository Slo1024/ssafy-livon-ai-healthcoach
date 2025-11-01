package com.s406.livon.global.error.handler;


import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.web.response.code.BaseErrorCode;

public class ChatHandler extends GeneralException {
  public ChatHandler(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
