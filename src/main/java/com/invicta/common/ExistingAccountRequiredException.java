package com.invicta.common;

public class ExistingAccountRequiredException extends RuntimeException {
    public ExistingAccountRequiredException() {
        super("该邮箱属于已有商户，请选择已有账户登录");
    }
}
