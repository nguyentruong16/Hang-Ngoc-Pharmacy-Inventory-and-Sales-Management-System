package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * One recent stock-movement row for the "Lịch sử tồn kho gần đây" preview block.
 *
 * <p>This is a lightweight preview assembled from import (Batch), sale (InvoiceDetail), stock-out
 * (StockOutDetail) and return (ReturnDetail) events — NOT the full Product Inventory History screen,
 * and it does not compute running balances. {@code occurredAt} is kept only for chronological
 * sorting; the template uses {@code timeDisplay}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecentHistoryResponse {
    /** Raw timestamp, used only to sort rows from the different sources. */
    private Instant occurredAt;
    private String timeDisplay;
    /** Movement type label: "Nhập kho" / "Bán hàng" / "Xuất kho - ..." / "Trả hàng". */
    private String changeType;
    private String branchName;
    /** Reference code of the source document (invoice code, stock-out code, batch name…). */
    private String reference;
    private String lotNumber;
    /** Signed base-unit quantity change: positive = stock in, negative = stock out. */
    private int quantityChange;
    private String note;
}
