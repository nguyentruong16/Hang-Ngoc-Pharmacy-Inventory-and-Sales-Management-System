package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class PurchaseInvoiceToBatchPageResponse {

    private Integer purchaseId;
    private String purchaseCode;

    private String dateDisplay;
    private String supplierName;
    private String employeeName;

    private BigDecimal totalAmount;

    private long productCount;
    private long batchToCreateCount;
    private long nearExpiryCount;

    private boolean allBatchCreated;

    private List<PurchaseInvoiceToBatchItemResponse> items;
}