package com.invicta.common;

public class TooManyCodeRequestsException extends RuntimeException {
    public TooManyCodeRequestsException() {
        super("请求频繁，请稍后再试");
    }
}
