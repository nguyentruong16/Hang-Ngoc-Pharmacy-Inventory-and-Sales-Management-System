package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A prospective adjustment line derived from one stock-count detail whose {@code actualQty} differs
 * from {@code systemQty}. Surplus (actual &gt; system) → {@code COUNT_INCREASE}/{@code IN}; shortage
 * → {@code COUNT_DECREASE}/{@code OUT}. Purely read-only preview data for the create screen; the
 * server rebuilds these authoritatively on submit and never trusts posted quantities. Belongs to
 * the stock-adjustment feature.
 */
@Getter
@AllArgsConstructor
public class StockAdjustmentCountLineResponse {

    private Integer batchId;

    private Integer productId;
    private String productName;

    private String lotNumber;

    private LocalDate expirationDate;
    private String expirationDateDisplay;

    private String unitName;

    private Integer systemQty;
    private Integer actualQty;

    /** Absolute discrepancy = the quantity to adjust (always &gt; 0). */
    private Integer quantity;

    /** COUNT_INCREASE or COUNT_DECREASE. */
    private String adjustmentType;
    /** IN for surplus, OUT for shortage. */
    private String direction;

    private BigDecimal unitCostPrice;
    private BigDecimal lineCost;
}
