package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class PurchaseInvoiceListItemResponse {

    private Integer id;
    private String purchaseCode;

    private Instant date;
    private String dateDisplay;

    private String supplierName;
    private String employeeName;

    private long totalItems;
    private BigDecimal totalAmount;
    private BigDecimal paid;
    private BigDecimal debtAmount;

    private String paymentStatus;
    private String statusCssClass;

    private boolean validForDeduction;
}