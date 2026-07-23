package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Form backing the "create expense" screen. Kept intentionally minimal (V1: a standalone cash-
 * outflow control tool, not a cross-module auto-triggered flow) — {@code applicantID} is always
 * the current user, and the optional {@code Return}/{@code PurchaseInvoice}/{@code ShiftReport}/
 * {@code Supplier}/{@code Customer}/{@code Account} links on the entity are left unset for now.
 */
@Getter
@Setter
public class ExpenseCreateRequest {

    /** One of {@link com.example.project.constant.ExpenseType}'s ALL values. */
    private String expenseType;

    /** yyyy-MM-dd from the date input; defaults to today when blank. */
    private String date;

    private String reason;

    /** Tổng số tiền cần chi. */
    private BigDecimal amount;

    /** Whether the full amount was already paid out at creation time. */
    private boolean fullyPaid = true;

    /** Only read when {@code fullyPaid} is false; must be between 0 and {@code amount}. */
    private BigDecimal paid;

    private BigDecimal paidByCash;

    private BigDecimal paidByBanking;

    private String note;
}
