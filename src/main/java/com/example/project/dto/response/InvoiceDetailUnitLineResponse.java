package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/** One sold unit line on the invoice detail page. */
@Getter
@AllArgsConstructor
public class InvoiceDetailUnitLineResponse {

    private Integer productUnitId;
    private String unitName;
    private boolean defaultUnit;
    private Integer quantity;
    private BigDecimal unitSellPrice;
    private BigDecimal lineSubtotal;
    private Integer returnedQty;
    /** Optional batch / expiry summary when stock was taken from one or more lots. */
    private String batchSummary;
}
