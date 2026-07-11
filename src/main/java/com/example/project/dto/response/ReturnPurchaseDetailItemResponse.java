package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/** One product line on the supplier-return detail screen. */
@Getter
@AllArgsConstructor
public class ReturnPurchaseDetailItemResponse {

    private Integer productId;
    private String productName;
    private String lotNumber;
    private String expirationDateDisplay;
    private String unitName;
    private Integer returnQty;
    private BigDecimal unitImportPrice;
    private BigDecimal lineRefund;
}
