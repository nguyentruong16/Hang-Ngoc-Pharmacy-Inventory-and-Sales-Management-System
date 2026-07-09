package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/** One product line on the customer-return detail screen. */
@Getter
@AllArgsConstructor
public class ReturnDetailItemResponse {

    private Integer productId;
    private String productName;

    private String lotNumber;
    private String expirationDateDisplay;

    private String unitName;

    private Integer returnQty;
    private BigDecimal unitSellPrice;
    private BigDecimal lineRefund;

    private boolean restockable;
    private String restockableDisplay;
}
