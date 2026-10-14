package com.invicta.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "统一分页响应，页码从 0 开始")
public record PageResponse<T>(
        @Schema(description = "当前页数据") List<T> items,
        @Schema(description = "符合条件的总记录数", example = "128") long total,
        @Schema(description = "当前页码，从 0 开始", example = "0") int page,
        @Schema(description = "每页记录数", example = "20") int size) {
    public static <T> PageResponse<T> of(List<T> items, long total, PageQuery query) {
        return new PageResponse<>(items, total, query.page(), query.size());
    }
}
