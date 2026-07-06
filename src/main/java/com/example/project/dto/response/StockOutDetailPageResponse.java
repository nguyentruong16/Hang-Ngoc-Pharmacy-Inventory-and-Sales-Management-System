package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class StockOutDetailPageResponse {

    private Integer id;
    private String stockOutCode;

    private Instant date;
    private String dateDisplay;

    private String outType;
    private String outTypeDisplay;

    private String createdByName;
    private String approvedByName;
    private String approvedAtDisplay;

    private String reason;
    private String note;

    private Integer statusId;
    private String statusName;
    private String statusCssClass;

    private long totalItems;
    private int totalQuantity;
    private BigDecimal estimatedValue;

    private String costImpactDisplay;

    private int accountingCheckDone;
    private int accountingCheckTotal;
    private int accountingCheckPercent;

    private boolean sourceChecked;
    private boolean targetChecked;
    private boolean valueChecked;
    private boolean reasonChecked;

    private List<StockOutDetailItemResponse> items;
}