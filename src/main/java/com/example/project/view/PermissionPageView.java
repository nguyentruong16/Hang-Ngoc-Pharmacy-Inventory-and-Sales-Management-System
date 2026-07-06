package com.example.project.view;

import lombok.Getter;

import java.util.List;

/**
 * The whole state of the (single-store) Owner permission screen for one render: the current page
 * of account rows and the active search. All pagination maths is pre-computed here so the
 * Thymeleaf template only reads simple values.
 *
 * <p>{@link #pageIndex} is 0-based (it equals the {@code page} request parameter); {@link
 * #getPageNumber()} / {@link #getTotalPages()} are 1-based for display.</p>
 */
@Getter
public class PermissionPageView {

    private final List<PermissionAccountRow> rows;
    private final int pageIndex;        // 0-based current page (= the "page" request param)
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final String search;

    public PermissionPageView(List<PermissionAccountRow> rows,
                              int pageIndex, int size, long totalElements, int totalPages,
                              String search) {
        this.rows = rows;
        this.pageIndex = pageIndex;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.search = search;
    }

    /** 1-based current page, for the "Trang {current} / {total}" label. */
    public int getPageNumber() {
        return pageIndex + 1;
    }

    public boolean isHasPrevious() {
        return pageIndex > 0;
    }

    public boolean isHasNext() {
        return pageIndex + 1 < totalPages;
    }

    /** Target page for the "Trước" link (never negative). */
    public int getPreviousPage() {
        return Math.max(0, pageIndex - 1);
    }

    /** Target page for the "Sau" link (never past the last page). */
    public int getNextPage() {
        return totalPages == 0 ? 0 : Math.min(totalPages - 1, pageIndex + 1);
    }

    /** 1-based index of the first row on this page (0 when there are no rows). */
    public long getFromIndex() {
        return totalElements == 0 ? 0 : (long) pageIndex * size + 1;
    }

    /** 1-based index of the last row on this page. */
    public long getToIndex() {
        return Math.min((long) (pageIndex + 1) * size, totalElements);
    }
}
