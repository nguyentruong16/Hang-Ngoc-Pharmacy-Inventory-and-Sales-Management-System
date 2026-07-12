package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PurchaseInvoicePrintLineResponse {

    private String productCode;
    private String productName;
    private BigDecimal importPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
}
