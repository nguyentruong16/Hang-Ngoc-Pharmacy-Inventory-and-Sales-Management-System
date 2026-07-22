package com.example.project.constant;

import java.util.List;

/**
 * Central definition of the valid {@code Purchaseinvoice.status} values, per
 * {@code docs/context/Pharmacy-Database-Description.docx}. The column is a plain
 * {@code varchar(50)} (no {@code Status} table, see CLAUDE.md) — these constants exist so the
 * vocabulary is written in exactly one place and never drifts between the service and templates.
 */
public final class PurchaseInvoiceStatus {

    public static final String DRAFT = "Nháp";
    public static final String DEBT = "Nợ";
    public static final String PARTIAL_DEBT = "Nợ một phần";
    public static final String COMPLETED = "Hoàn thành";

    /**
     * Chứng từ nội bộ, không phải hóa đơn đã xuất — không chịu ràng buộc luật cấm hủy hóa đơn.
     * Hủy chỉ dùng để sửa lỗi lập sai; lý do hủy được ghi vào {@code Purchaseinvoice.note}.
     */
    public static final String CANCELLED = "Đã hủy";

    /** All valid statuses, in workflow order. */
    public static final List<String> ALL = List.of(DRAFT, DEBT, PARTIAL_DEBT, COMPLETED, CANCELLED);

    private PurchaseInvoiceStatus() {
    }
}
