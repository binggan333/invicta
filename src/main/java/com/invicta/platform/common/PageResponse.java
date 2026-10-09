package com.invicta.platform.common;

import java.util.List;

public record PageResponse<T>(List<T> items, long total, int page, int size) {
    public static <T> PageResponse<T> of(List<T> items, long total, PageQuery query) {
        return new PageResponse<>(items, total, query.page(), query.size());
    }
}
