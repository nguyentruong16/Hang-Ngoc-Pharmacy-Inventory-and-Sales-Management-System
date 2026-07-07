package com.example.project.constant;

import java.util.List;

/**
 * Central definition of the valid {@code Stockout.status} values, per
 * {@code docs/context/Pharmacy-Database-Description.docx}. The column is a plain
 * {@code varchar(50)} (no {@code Status} table, see CLAUDE.md) — these constants exist so the
 * vocabulary is written in exactly one place and never drifts between the service, controller and
 * templates.
 */
public final class StockOutStatus {

    public static final String DRAFT = "Nháp";
    public static final String REJECTED = "Từ chối";
    public static final String APPROVED = "Duyệt";

    /** All valid statuses, in workflow order (for the filter dropdown). */
    public static final List<String> ALL = List.of(DRAFT, REJECTED, APPROVED);

    private StockOutStatus() {
    }
}
