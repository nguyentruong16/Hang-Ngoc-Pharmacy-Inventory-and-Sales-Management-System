package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/** Product block on the invoice detail page (multiple sale units per product). */
@Getter
@AllArgsConstructor
public class InvoiceDetailProductGroupResponse {

    private Integer productId;
    private String productCode;
    private String productName;
    private BigDecimal productSubtotal;
    private List<InvoiceDetailUnitLineResponse> lines;
}
