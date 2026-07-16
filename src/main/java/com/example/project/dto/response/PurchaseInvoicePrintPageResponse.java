package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class PurchaseInvoicePrintPageResponse {

    private Integer purchaseId;
    private String purchaseCode;
    private String dateDisplay;

    private String createdByName;
    private String supplierName;
    private String supplierAddress;

    private int totalQuantity;
    private BigDecimal subtotal;
    private BigDecimal additionCost;
    private BigDecimal discount;
    private BigDecimal totalVATInput;
    private BigDecimal totalAmount;

    private String vatInvoiceNumber;
    private String vatInvoiceDateDisplay;

    private String note;

    private List<PurchaseInvoicePrintLineResponse> lines;
}
