package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class PurchaseInvoiceDetailPageResponse {

    private Integer id;
    private String purchaseCode;

    private Instant date;
    private String dateDisplay;

    private String supplierName;
    private String supplierPhone;
    private String supplierEmail;

    private String employeeName;

    private String procurementCode;

    private BigDecimal subtotal;
    private BigDecimal additionCost;
    private BigDecimal discount;
    private BigDecimal totalVATInput;
    private BigDecimal totalAmount;
    private BigDecimal paid;
    private BigDecimal debtAmount;

    private String paymentStatus;
    private String statusCssClass;

    private String vatInvoiceNumber;
    private String vatInvoiceDateDisplay;

    private String dueDateDisplay;
    private boolean validForDeduction;

    private String note;

    private long totalItems;
    private int totalQuantity;

    private List<PurchaseInvoiceDetailItemResponse> items;
}