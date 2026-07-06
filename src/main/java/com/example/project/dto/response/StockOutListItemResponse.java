package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class StockOutListItemResponse {

    private Integer id;
    private String stockOutCode;
    private Instant date;
    private String dateDisplay;

    private String outType;
    private String outTypeDisplay;

    private String createdByName;

    private long totalItems;
    private BigDecimal estimatedValue;

    private Integer statusId;
    private String statusName;
    private String statusCssClass;
}