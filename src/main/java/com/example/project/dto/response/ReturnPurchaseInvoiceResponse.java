package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * One purchase invoice the store may still return goods against, for the "choose purchase" modal on
 * the create screen. Read-only over the purchasing module.
 */
@Getter
@AllArgsConstructor
public class ReturnPurchaseInvoiceResponse {

    private Integer purchaseId;
    private String purchaseCode;
    private String dateDisplay;
    private String supplierName;
    private String employeeName;
    private BigDecimal totalAmount;

    /** Number of lines that still have on-hand stock to return. */
    private int returnableLineCount;
    private String returnStatusDisplay;
}
