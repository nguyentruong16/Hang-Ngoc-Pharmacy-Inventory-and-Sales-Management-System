package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProcurementPlanPrintPageResponse {

    private Integer planId;
    private String procurementCode;
    private String dateDisplay;
    private String status;
    private String note;
    private int totalItems;
    private BigDecimal totalEstimatedAmount;
    private List<ProcurementPlanPrintLineResponse> lines;
}
