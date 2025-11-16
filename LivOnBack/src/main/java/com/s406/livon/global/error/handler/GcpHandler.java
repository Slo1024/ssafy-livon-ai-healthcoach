package com.s406.livon.global.error.handler;

import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.web.response.code.BaseErrorCode;

/**
 * GCP 관련 예외 핸들러
 */
public class GcpHandler extends GeneralException {

    public GcpHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}

