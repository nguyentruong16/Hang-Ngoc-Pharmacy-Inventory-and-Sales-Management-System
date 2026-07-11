package com.example.project.constant;

import java.util.List;

/**
 * Valid {@code Return.status} values for a <em>supplier</em> return (a return slip whose
 * {@code purchaseID} is set and {@code invoiceID} is null).
 *
 * <p>Supplier returns are Owner-only (Pharmacist has no rights on the supplier side), so the
 * customer-return "Chờ duyệt" hand-off does not apply. The Owner either saves a draft or approves
 * immediately; approval is where stock is deducted and the amounts are recorded.</p>
 *
 * <ul>
 *   <li>{@link #DRAFT} — a work-in-progress slip not yet applied.</li>
 *   <li>{@link #APPROVED} — approved by the Owner; stock has been deducted from the purchase's
 *       batches and the refund amounts recorded. (Unlike a customer return, which lands in "Nợ"
 *       because the pharmacy owes the customer, a supplier return brings money back <em>in</em>, so
 *       the neutral "Đã duyệt" is used. The actual Income voucher is a later phase.)</li>
 *   <li>{@link #REJECTED} — the Owner abandoned/declined the draft. No stock change.</li>
 * </ul>
 *
 * <p>Kept separate from {@link ReturnStatus} (customer) on purpose: the two workflows share the
 * {@code return} table but have different vocabularies.</p>
 */
public final class ReturnPurchaseStatus {

    public static final String DRAFT = "Nháp";
    public static final String APPROVED = "Đã duyệt";
    public static final String REJECTED = "Từ chối";

    /** All valid statuses, in workflow order (for the filter dropdown). */
    public static final List<String> ALL = List.of(DRAFT, APPROVED, REJECTED);

    private ReturnPurchaseStatus() {
    }
}
