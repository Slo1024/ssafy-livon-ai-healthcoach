package com.s406.livon.global.error.handler;


import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.web.response.code.BaseErrorCode;

public class UserHandler extends GeneralException {

    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
