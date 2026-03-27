package cv.igrp.RH_Service.shared.domain.pagination;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResult<T> {

    private final List<T> data;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public PageResult(List<T> data, int pageNumber, int pageSize,
            long totalElements, int totalPages,
            boolean first, boolean last) {
        this.data = data;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }
}
