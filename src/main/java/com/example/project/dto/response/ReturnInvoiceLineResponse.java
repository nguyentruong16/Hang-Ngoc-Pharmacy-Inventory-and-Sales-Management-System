package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * One returnable line of a chosen invoice, fetched as JSON by the create screen after the user picks
 * an invoice. Carries the still-returnable quantity so the UI can cap the input.
 */
@Getter
@AllArgsConstructor
public class ReturnInvoiceLineResponse {

    private Integer invoiceDetailId;

    private Integer productId;
    private String productName;

    private String lotNumber;
    private String expirationDateDisplay;

    private String unitName;

    /** Units originally sold on this line. */
    private Integer soldQty;
    /** Units already returned across prior returns. */
    private Integer returnedQty;
    /** {@code soldQty - returnedQty} — the max this line can still return. */
    private Integer returnableQty;

    private BigDecimal unitSellPrice;

    /**
     * Whether this line was sold in the product's base unit (e.g. "hộp"). Only base-unit lines can be
     * cleanly restocked, so this drives the default (and the enable state) of the restock checkbox.
     */
    private boolean soldInBaseUnit;
}
