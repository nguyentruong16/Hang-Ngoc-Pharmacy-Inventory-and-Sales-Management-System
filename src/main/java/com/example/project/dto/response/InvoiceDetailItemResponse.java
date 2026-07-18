package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/** One line on the sale-invoice detail page. */
@Getter
@AllArgsConstructor
public class InvoiceDetailItemResponse {

    private Integer productId;
    private String productCode;
    private String productName;
    private String batchCode;
    private String expirationDateDisplay;
    private String unitName;
    private Integer quantity;
    private BigDecimal unitSellPrice;
    private BigDecimal subtotal;
    private BigDecimal vatRate;
    private BigDecimal preTaxAmount;
    private BigDecimal vatAmount;
    private Integer returnedQty;
}
