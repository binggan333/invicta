package com.invicta.common;

import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;

/**
 * Invicta 对外错误码目录。code 表达具体错误原因，HTTP 状态表达错误类别。
 */
public enum ApiErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "error.invalid-request", "请求格式或参数值不正确"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "error.validation-failed", "请求字段校验失败，详见 details"),
    VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "error.verification-code-invalid", "验证码错误、过期或已使用"),
    EXPORT_TIME_ORDER_INVALID(HttpStatus.BAD_REQUEST, "error.export.time-order", "导出结束时间不晚于开始时间"),
    EXPORT_RANGE_EXCEEDED(HttpStatus.BAD_REQUEST, "error.export.range-exceeded", "导出时间范围超过一年"),
    EXPORT_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "error.export.count-exceeded", "导出记录超过 100000 条"),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "error.authentication-required", "未提供有效的登录令牌"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "error.invalid-credentials", "邮箱或密码错误"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "error.forbidden", "没有目标资源的访问权限"),
    PROFILE_INCOMPLETE(HttpStatus.FORBIDDEN, "error.profile-incomplete", "商户资料尚未完善"),
    ACCOUNT_UNAVAILABLE(HttpStatus.NOT_FOUND, "error.account-unavailable", "登录邮箱未注册或账号不可用"),
    EXISTING_ACCOUNT_UNAVAILABLE(HttpStatus.NOT_FOUND, "error.existing-account-unavailable", "历史商户名不能唯一定位可用账号"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "error.payment.not-found", "收单记录不存在"),
    RECHARGE_NOT_FOUND(HttpStatus.NOT_FOUND, "error.recharge.not-found", "充值记录不存在"),
    ORGANIZATION_NOT_FOUND(HttpStatus.NOT_FOUND, "error.organization.not-found", "商户账户不存在"),
    SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "error.summary.not-found", "归集记录不存在"),
    BALANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "error.balance.not-found", "平衡记录不存在"),
    WITHDRAW_NOT_FOUND(HttpStatus.NOT_FOUND, "error.withdraw.not-found", "提现记录不存在"),
    PREPAID_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "error.prepaid.account-not-found", "预付费账户不存在"),
    STRATEGY_NOT_FOUND(HttpStatus.NOT_FOUND, "error.strategy.not-found", "指定链的商户策略不存在"),
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "error.email-already-registered", "邮箱已作为 Invicta 登录邮箱注册"),
    EXISTING_ACCOUNT_REQUIRED(HttpStatus.CONFLICT, "error.existing-account-required", "邮箱属于历史商户，应选择已有账户登录"),
    CODE_REQUEST_TOO_FREQUENT(HttpStatus.TOO_MANY_REQUESTS, "error.code-request-too-frequent", "验证码发送冷却时间未结束"),
    VERIFICATION_EMAIL_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "error.verification-email-unavailable", "验证码邮件发送失败"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.internal-server", "服务内部异常"),
    REQUEST_FAILED(HttpStatus.BAD_REQUEST, "error.request-failed", "未分类的业务请求失败");

    private final HttpStatus status;
    private final String messageKey;
    private final String meaning;

    ApiErrorCode(HttpStatus status, String messageKey, String meaning) {
        this.status = status;
        this.messageKey = messageKey;
        this.meaning = meaning;
    }

    public String code() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public HttpStatus status() {
        return status;
    }

    public String messageKey() {
        return messageKey;
    }

    public String meaning() {
        return meaning;
    }

    public static Optional<ApiErrorCode> forReason(String reason) {
        return Arrays.stream(values()).filter(code -> code.messageKey.equals(reason)).findFirst();
    }
}
