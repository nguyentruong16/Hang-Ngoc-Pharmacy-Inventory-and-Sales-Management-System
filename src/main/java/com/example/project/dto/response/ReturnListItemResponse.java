package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/** One row in the customer-return list. */
@Getter
@AllArgsConstructor
public class ReturnListItemResponse {

    private Integer id;
    private String code;

    private Instant date;
    private String dateDisplay;

    private String invoiceCode;
    private String customerName;

    private String createdByName;

    private long totalItems;
    private BigDecimal totalRefund;

    private String returnType;
    private String returnTypeDisplay;

    private String statusName;
    private String statusCssClass;
}
