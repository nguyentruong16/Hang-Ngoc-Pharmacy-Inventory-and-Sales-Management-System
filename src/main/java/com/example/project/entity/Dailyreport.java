package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "dailyreport")
public class Dailyreport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reportID", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branchID", nullable = false)
    private Branch branchID;

    @NotNull
    @Column(name = "reportDate", nullable = false)
    private LocalDate reportDate;

    @ColumnDefault("0")
    @Column(name = "totalInvoicesNumber")
    private Integer totalInvoicesNumber;

    @ColumnDefault("0")
    @Column(name = "totalPrescriptionInvoices")
    private Integer totalPrescriptionInvoices;

    @ColumnDefault("0")
    @Column(name = "totalNormalInvoices")
    private Integer totalNormalInvoices;

    @Column(name = "totalIncomeCash", precision = 15, scale = 2)
    private BigDecimal totalIncomeCash;

    @Column(name = "totalIncomeBanking", precision = 15, scale = 2)
    private BigDecimal totalIncomeBanking;

    @Column(name = "totalIncomeDebt", precision = 15, scale = 2)
    private BigDecimal totalIncomeDebt;

    @ColumnDefault("0.00")
    @Column(name = "totalOtherIncome", precision = 15, scale = 2)
    private BigDecimal totalOtherIncome;

    @ColumnDefault("0")
    @Column(name = "totalReturnCount")
    private Integer totalReturnCount;

    @ColumnDefault("0.00")
    @Column(name = "totalReturnAmount", precision = 15, scale = 2)
    private BigDecimal totalReturnAmount;

    @ColumnDefault("0.00")
    @Column(name = "totalVATCollected", precision = 15, scale = 2)
    private BigDecimal totalVATCollected;

    @ColumnDefault("0.00")
    @Column(name = "totalSubimport", precision = 15, scale = 2)
    private BigDecimal totalSubimport;

    @NotNull
    @Column(name = "totalRevenue", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @ColumnDefault("0.00")
    @Column(name = "grossProfit", precision = 15, scale = 2)
    private BigDecimal grossProfit;

    @ColumnDefault("0.00")
    @Column(name = "totalExpenseByCash", precision = 15, scale = 2)
    private BigDecimal totalExpenseByCash;

    @ColumnDefault("0.00")
    @Column(name = "totalExpenseByBanking", precision = 15, scale = 2)
    private BigDecimal totalExpenseByBanking;

    @Column(name = "totalExpense", precision = 15, scale = 2)
    private BigDecimal totalExpense;

    @ColumnDefault("0.00")
    @Column(name = "totalDebtCreated", precision = 15, scale = 2)
    private BigDecimal totalDebtCreated;

    @ColumnDefault("0.00")
    @Column(name = "totalDebtCollected", precision = 15, scale = 2)
    private BigDecimal totalDebtCollected;

    @Column(name = "openingBalanceInCash", precision = 15, scale = 2)
    private BigDecimal openingBalanceInCash;

    @Column(name = "closingBalanceInCash", precision = 15, scale = 2)
    private BigDecimal closingBalanceInCash;

    @Column(name = "openingBalanceInBanking", precision = 15, scale = 2)
    private BigDecimal openingBalanceInBanking;

    @Column(name = "closingBalanceInBanking", precision = 15, scale = 2)
    private BigDecimal closingBalanceInBanking;

    @Column(name = "actualCashCounted", precision = 15, scale = 2)
    private BigDecimal actualCashCounted;

    @Column(name = "cashDiscrepancy", precision = 15, scale = 2)
    private BigDecimal cashDiscrepancy;

    @Column(name = "noteDiscrepancy", columnDefinition = "TEXT")
    private String noteDiscrepancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeID")
    private Account employeeID;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusID")
    private Status statusID;

    @Column(name = "approvedAt")
    private Instant approvedAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
