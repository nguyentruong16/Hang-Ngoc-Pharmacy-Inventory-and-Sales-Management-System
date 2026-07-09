package com.example.project.constant;

import java.util.List;

/**
 * Central definition of the valid {@code Return.status} values for a <em>customer</em> return.
 *
 * <p>The column is a plain {@code varchar(50)} (there is no {@code Status} table — confirmed by
 * {@code Pharmacy Database Description.docx}). These constants keep the vocabulary in one place so
 * it never drifts between the service, controller and templates.</p>
 *
 * <p>Workflow (owner-confirmed 2026-07-09):
 * <ul>
 *   <li>{@link #DRAFT} — a work-in-progress slip the creator has not sent yet.</li>
 *   <li>{@link #PENDING} — a Pharmacist has submitted it and it is awaiting the Owner's approval.</li>
 *   <li>{@link #DEBT} — approved. There is intentionally no "Duyệt" state: once approved the pharmacy
 *       <em>owes the customer</em> the (not-yet-paid) refund, so the slip lands in "Nợ". This is where
 *       stock is actually restored and the original invoice's return status is updated. Owner-created
 *       slips are auto-approved straight to {@link #DEBT}.</li>
 *   <li>{@link #REJECTED} — the Owner declined a pending slip.</li>
 * </ul>
 *
 * <p>The actual cash payout + shift linkage lives on a separate Expense (phiếu chi), handled in a
 * later phase; this vocabulary covers only the return slip itself.</p>
 */
public final class ReturnStatus {

    public static final String DRAFT = "Nháp";
    public static final String PENDING = "Chờ duyệt";
    public static final String DEBT = "Nợ";
    public static final String REJECTED = "Từ chối";

    /** All valid statuses, in workflow order (for the filter dropdown). */
    public static final List<String> ALL = List.of(DRAFT, PENDING, DEBT, REJECTED);

    private ReturnStatus() {
    }
}
