package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "邮箱验证码用途：REGISTRATION 用于注册，LOGIN 用于已注册账号登录；不同用途的验证码不可混用")
public enum EmailCodePurpose {
    LOGIN,
    REGISTRATION
}
