package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "邮箱验证码用途：LOGIN 登录，REGISTRATION 注册")
public enum EmailCodePurpose {
    LOGIN,
    REGISTRATION
}
