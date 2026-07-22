package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * One returnable line of a chosen purchase invoice, fetched as JSON by the create screen after the
 * user picks a purchase. Carries the current on-hand stock so the UI can cap the input.
 */
@Getter
@AllArgsConstructor
public class ReturnPurchaseLineResponse {

    private Integer purchaseDetailId;

    private Integer productId;
    private String productName;

    private String lotNumber;
    private String expirationDateDisplay;

    private String unitName;

    /** Units originally imported on this line. */
    private Integer importedQty;
    /** Units already returned to the supplier across prior approved returns. */
    private Integer alreadyReturned;
    /** Current on-hand stock across the line's batches — the max this line can still return. */
    private Integer returnableQty;

    /** NET import price per import-unit (chưa thuế, theo tài liệu sheet 10) — hiển thị cột "Đơn giá nhập". */
    private BigDecimal importPricePerBase;

    /** GROSS refund unit price = net + input VAT — NCC hoàn 100%; cơ sở cột "Tiền hoàn". */
    private BigDecimal refundUnitPrice;
}
