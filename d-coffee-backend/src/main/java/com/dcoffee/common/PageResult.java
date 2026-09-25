package com.dcoffee.common;

import lombok.Getter;

import java.util.List;

@Getter
public final class PageResult<T> {
    private final List<T> records;
    private final long total;
    private final int page;
    private final int pageSize;

    public PageResult(List<T> records, long total, int page, int pageSize) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }
}
