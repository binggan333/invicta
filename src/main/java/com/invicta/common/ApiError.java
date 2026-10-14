package com.invicta.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "统一错误响应")
public record ApiError(
        @Schema(description = "稳定的机器可读错误码", example = "validation_failed") String code,
        @Schema(description = "按请求语言返回的错误提示", example = "请求参数校验失败") String message,
        @Schema(description = "错误发生时间（UTC）") Instant timestamp,
        @Schema(description = "请求追踪 ID，可用于日志检索") String requestId,
        @Schema(description = "字段级错误详情；没有详情时为空对象") Map<String, String> details) {}
