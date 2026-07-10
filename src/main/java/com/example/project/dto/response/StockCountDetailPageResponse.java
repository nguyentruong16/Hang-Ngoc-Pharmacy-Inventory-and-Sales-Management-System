package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class StockCountDetailPageResponse {

    private Integer stockCountId;

    private String stockCountCode;

    private Instant countDate;

    private String countDateDisplay;

    private String createdByName;

    private String approvedByName;

    private String approvedAtDisplay;

    private String status;

    private String statusCssClass;

    private String note;

    private long totalItems;

    private long matchedItems;

    private long discrepancyItems;

    private int totalSystemQty;

    private int totalActualQty;

    private int totalDiscrepancy;

    private List<StockCountDetailItemResponse> items;
}