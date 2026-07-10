package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class StockCountListItemResponse {

    private Integer stockCountId;

    private String stockCountCode;

    private Instant countDate;

    private String countDateDisplay;

    private String createdByName;

    private String approvedByName;

    private String approvedAtDisplay;

    private long totalItems;

    private long discrepancyItems;

    private String status;

    private String statusCssClass;

    private String note;
}