package com.invicta.common;

public class VerificationCodeInvalidException extends RuntimeException {
    public VerificationCodeInvalidException() {
        super("验证码已失效");
    }
}
