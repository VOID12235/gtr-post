package com.btr.platform.common;

public enum ErrorCode {
    BAD_REQUEST(40001, "参数错误"),
    UNAUTHORIZED(40101, "未登录"),
    LOGIN_FAILED(40102, "账号或密码错误"),
    FORBIDDEN(40300, "无权访问"),
    NOT_FOUND(40401, "资源不存在"),
    TENANT_NOT_FOUND(40402, "租户不存在或已停用"),
    CONFLICT(40901, "状态冲突"),
    INTERNAL(50001, "服务内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
