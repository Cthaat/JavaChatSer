package com.example.javachat.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    SUCCESS(0, "success", HttpStatus.OK),
    BAD_REQUEST(40000, "参数错误", HttpStatus.BAD_REQUEST),
    LOGIN_FAILED(40001, "用户名或密码错误", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(40100, "未登录或 Token 无效", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(40300, "无权限", HttpStatus.FORBIDDEN),
    NOT_FOUND(40400, "数据不存在", HttpStatus.NOT_FOUND),
    CONFLICT(40900, "数据冲突", HttpStatus.CONFLICT),
    SERVER_ERROR(50000, "服务端错误", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
