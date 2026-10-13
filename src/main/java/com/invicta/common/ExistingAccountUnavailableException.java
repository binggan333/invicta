package com.invicta.common;

public class ExistingAccountUnavailableException extends RuntimeException {
    public ExistingAccountUnavailableException() {
        super("商户名称不存在、不唯一或账户不可用");
    }
}
