package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PurchaseInvoiceToBatchItemResponse {

    private Integer purchaseDetailId;

    private String productId;
    private String productName;

    private Integer quantity;
    private String unitName;
    private BigDecimal importPrice;

    private String lotNumber;

    private LocalDate productionDate;
    private String productionDateDisplay;

    private LocalDate expirationDate;
    private String expirationDateDisplay;

    private boolean batchCreated;

    private String statusDisplay;
    private String statusCssClass;
}