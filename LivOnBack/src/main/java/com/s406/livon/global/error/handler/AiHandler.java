package com.s406.livon.global.error.handler;

import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.web.response.code.BaseErrorCode;

public class AiHandler extends GeneralException {

    public AiHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
