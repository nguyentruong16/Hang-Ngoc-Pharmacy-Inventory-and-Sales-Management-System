package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ShiftReportDetailPageResponse {

    private Integer id;

    private String shiftReportCode;

    private Integer cashierId;

    private String cashierName;

    private String shiftDateDisplay;

    private String shiftType;

    private String startTimeDisplay;

    private String endTimeDisplay;

    private BigDecimal openingCash;

    private Integer totalInvoices;

    private BigDecimal totalRevenue;

    private Integer totalReturns;

    private BigDecimal totalReturnAmount;

    private BigDecimal totalDebtCollected;

    private BigDecimal totalCashIn;

    private BigDecimal totalBankingIn;

    private BigDecimal totalCashOut;

    private BigDecimal expectedClosingCash;

    private BigDecimal actualClosingCash;

    private BigDecimal cashDiscrepancy;

    private String noteDiscrepancy;

    private String status;

    private String statusCssClass;

    private String approvedAtDisplay;

    private String note;

    private boolean canClose;
}
