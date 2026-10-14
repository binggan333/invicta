package com.invicta.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * UTC Pay 写接口的统一响应。
 */
@Schema(description = "UTC Pay 写操作响应")
public record LegacyApiResponse(
        @Schema(description = "UTC Pay 业务状态码", example = "0") Integer code,
        @Schema(description = "结果提示") String msg,
        @Schema(description = "补充说明") String description,
        @Schema(description = "业务结果；结构由具体 UTC Pay 接口决定") Object data) {}
