package com.example.project.dto.response;

import com.example.project.entity.Dailyreport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyreportResponse {
    private Integer id;
    private String dailyReportCode;
    private Integer branchId;
    private LocalDate reportDate;
    private Integer totalInvoicesNumber;
    private Integer totalPrescriptionInvoices;
    private Integer totalNormalInvoices;
    private BigDecimal totalIncomeCash;
    private BigDecimal totalIncomeBanking;
    private BigDecimal totalIncomeDebt;
    private BigDecimal totalOtherIncome;
    private Integer totalReturnCount;
    private BigDecimal totalReturnAmount;
    private BigDecimal totalVATCollected;
    private BigDecimal totalSubimport;
    private BigDecimal totalRevenue;
    private BigDecimal grossProfit;
    private BigDecimal totalExpenseByCash;
    private BigDecimal totalExpenseByBanking;
    private BigDecimal totalExpense;
    private BigDecimal totalDebtCreated;
    private BigDecimal totalDebtCollected;
    private BigDecimal openingBalanceInCash;
    private BigDecimal closingBalanceInCash;
    private BigDecimal openingBalanceInBanking;
    private BigDecimal closingBalanceInBanking;
    private BigDecimal actualCashCounted;
    private BigDecimal cashDiscrepancy;
    private String noteDiscrepancy;
    private Integer employeeId;
    private Instant createdAt;
    private Integer statusId;
    private Instant approvedAt;
    private String note;

    public static DailyreportResponse from(Dailyreport dailyreport) {
        return new DailyreportResponse(
                dailyreport.getId(),
                dailyreport.getDailyReportCode(),
                dailyreport.getBranchID() != null ? dailyreport.getBranchID().getId() : null,
                dailyreport.getReportDate(),
                dailyreport.getTotalInvoicesNumber(),
                dailyreport.getTotalPrescriptionInvoices(),
                dailyreport.getTotalNormalInvoices(),
                dailyreport.getTotalIncomeCash(),
                dailyreport.getTotalIncomeBanking(),
                dailyreport.getTotalIncomeDebt(),
                dailyreport.getTotalOtherIncome(),
                dailyreport.getTotalReturnCount(),
                dailyreport.getTotalReturnAmount(),
                dailyreport.getTotalVATCollected(),
                dailyreport.getTotalSubimport(),
                dailyreport.getTotalRevenue(),
                dailyreport.getGrossProfit(),
                dailyreport.getTotalExpenseByCash(),
                dailyreport.getTotalExpenseByBanking(),
                dailyreport.getTotalExpense(),
                dailyreport.getTotalDebtCreated(),
                dailyreport.getTotalDebtCollected(),
                dailyreport.getOpeningBalanceInCash(),
                dailyreport.getClosingBalanceInCash(),
                dailyreport.getOpeningBalanceInBanking(),
                dailyreport.getClosingBalanceInBanking(),
                dailyreport.getActualCashCounted(),
                dailyreport.getCashDiscrepancy(),
                dailyreport.getNoteDiscrepancy(),
                dailyreport.getEmployeeID() != null ? dailyreport.getEmployeeID().getId() : null,
                dailyreport.getCreatedAt(),
                dailyreport.getStatusID() != null ? dailyreport.getStatusID().getId() : null,
                dailyreport.getApprovedAt(),
                dailyreport.getNote()
        );
    }
}