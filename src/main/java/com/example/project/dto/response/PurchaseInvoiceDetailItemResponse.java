package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PurchaseInvoiceDetailItemResponse {

    private Integer productId;
    private String productName;
    private String lotNumber;
    private LocalDate productionDate;
    private String productionDateDisplay;
    private LocalDate expirationDate;
    private String expirationDateDisplay;
    private Integer quantity;
    private BigDecimal importPrice;
    private BigDecimal lineTotal;
}