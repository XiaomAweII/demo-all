package com.maxiaowei.demo001.common;

public class ServiceException extends RuntimeException {
    private String code;

    public ServiceException(String message, String code) {
        super(message);
        this.code = code;
    }
}
