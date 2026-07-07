package com.example.project.constant;

import java.util.List;

/**
 * Central definition of the valid {@code Stockadjustment.status} values.
 *
 * <p>The column is a plain {@code varchar(50)} (there is no {@code Status} table for stock
 * adjustments — confirmed by {@code Pharmacy Database Description.docx}). These constants exist so
 * the vocabulary is written in exactly one place and never drifts between the service, controller
 * and templates.</p>
 *
 * <p>Workflow: a Pharmacist-created slip starts as {@link #PENDING}; an Owner-created slip is
 * auto-approved straight to {@link #APPROVED}. From {@link #PENDING} the Owner either
 * {@link #APPROVED approves} or {@link #REJECTED rejects}.</p>
 */
public final class StockAdjustmentStatus {

    public static final String PENDING = "Chờ duyệt";
    public static final String REJECTED = "Từ chối";
    public static final String APPROVED = "Duyệt";

    /** All valid statuses, in workflow order (for the filter dropdown). */
    public static final List<String> ALL = List.of(PENDING, APPROVED, REJECTED);

    private StockAdjustmentStatus() {
    }
}
