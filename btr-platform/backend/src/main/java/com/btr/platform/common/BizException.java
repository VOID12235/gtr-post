package com.btr.platform.common;

public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode errorCode) {
        this(errorCode.code(), errorCode.message());
    }

    public BizException(ErrorCode errorCode, String message) {
        this(errorCode.code(), message);
    }

    public int getCode() {
        return code;
    }
}
