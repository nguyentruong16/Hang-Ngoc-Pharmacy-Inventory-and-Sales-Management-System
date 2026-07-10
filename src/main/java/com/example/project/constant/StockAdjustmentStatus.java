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
 * <p>Workflow:
 * <ul>
 *   <li>{@link #DRAFT} — a work-in-progress slip the creator has not sent yet.</li>
 *   <li>{@link #PENDING} — a slip a Pharmacist has submitted and is awaiting the Owner's approval.</li>
 *   <li>{@link #APPROVED} — approved by the Owner (this also covers an Owner-created slip, which is
 *       auto-approved on submit). This is the step that actually moves stock: {@code batch.storageQuantity}
 *       is increased (IN) or decreased (OUT) for each line. (The stock <em>count</em> screen is the
 *       read-only step that does not move stock.)</li>
 *   <li>{@link #REJECTED} — the Owner declined a pending slip.</li>
 * </ul>
 */
public final class StockAdjustmentStatus {

    public static final String DRAFT = "Nháp";
    public static final String PENDING = "Chờ duyệt";
    public static final String REJECTED = "Từ chối";
    public static final String APPROVED = "Duyệt";

    /** All valid statuses, in workflow order (for the filter dropdown). */
    public static final List<String> ALL = List.of(DRAFT, PENDING, APPROVED, REJECTED);

    private StockAdjustmentStatus() {
    }
}
