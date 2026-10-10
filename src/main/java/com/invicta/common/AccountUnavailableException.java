package com.invicta.common;

public class AccountUnavailableException extends RuntimeException {
    public AccountUnavailableException() {
        super("邮箱尚未注册或机构账号不可用");
    }
}
