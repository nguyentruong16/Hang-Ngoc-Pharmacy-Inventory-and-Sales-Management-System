package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "dailyreport")
public class Dailyreport {
    @Id
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

    @Lob
    @Column(name = "noteDiscrepancy")
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

    @Lob
    @Column(name = "note")
    private String note;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Branch getBranchID() {
        return branchID;
    }

    public void setBranchID(Branch branchID) {
        this.branchID = branchID;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public Integer getTotalInvoicesNumber() {
        return totalInvoicesNumber;
    }

    public void setTotalInvoicesNumber(Integer totalInvoicesNumber) {
        this.totalInvoicesNumber = totalInvoicesNumber;
    }

    public Integer getTotalPrescriptionInvoices() {
        return totalPrescriptionInvoices;
    }

    public void setTotalPrescriptionInvoices(Integer totalPrescriptionInvoices) {
        this.totalPrescriptionInvoices = totalPrescriptionInvoices;
    }

    public Integer getTotalNormalInvoices() {
        return totalNormalInvoices;
    }

    public void setTotalNormalInvoices(Integer totalNormalInvoices) {
        this.totalNormalInvoices = totalNormalInvoices;
    }

    public BigDecimal getTotalIncomeCash() {
        return totalIncomeCash;
    }

    public void setTotalIncomeCash(BigDecimal totalIncomeCash) {
        this.totalIncomeCash = totalIncomeCash;
    }

    public BigDecimal getTotalIncomeBanking() {
        return totalIncomeBanking;
    }

    public void setTotalIncomeBanking(BigDecimal totalIncomeBanking) {
        this.totalIncomeBanking = totalIncomeBanking;
    }

    public BigDecimal getTotalIncomeDebt() {
        return totalIncomeDebt;
    }

    public void setTotalIncomeDebt(BigDecimal totalIncomeDebt) {
        this.totalIncomeDebt = totalIncomeDebt;
    }

    public BigDecimal getTotalOtherIncome() {
        return totalOtherIncome;
    }

    public void setTotalOtherIncome(BigDecimal totalOtherIncome) {
        this.totalOtherIncome = totalOtherIncome;
    }

    public BigDecimal getTotalSubimport() {
        return totalSubimport;
    }

    public void setTotalSubimport(BigDecimal totalSubimport) {
        this.totalSubimport = totalSubimport;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }

    public BigDecimal getTotalExpenseByCash() {
        return totalExpenseByCash;
    }

    public void setTotalExpenseByCash(BigDecimal totalExpenseByCash) {
        this.totalExpenseByCash = totalExpenseByCash;
    }

    public BigDecimal getTotalExpenseByBanking() {
        return totalExpenseByBanking;
    }

    public void setTotalExpenseByBanking(BigDecimal totalExpenseByBanking) {
        this.totalExpenseByBanking = totalExpenseByBanking;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getOpeningBalanceInCash() {
        return openingBalanceInCash;
    }

    public void setOpeningBalanceInCash(BigDecimal openingBalanceInCash) {
        this.openingBalanceInCash = openingBalanceInCash;
    }

    public BigDecimal getClosingBalanceInCash() {
        return closingBalanceInCash;
    }

    public void setClosingBalanceInCash(BigDecimal closingBalanceInCash) {
        this.closingBalanceInCash = closingBalanceInCash;
    }

    public BigDecimal getOpeningBalanceInBanking() {
        return openingBalanceInBanking;
    }

    public void setOpeningBalanceInBanking(BigDecimal openingBalanceInBanking) {
        this.openingBalanceInBanking = openingBalanceInBanking;
    }

    public BigDecimal getClosingBalanceInBanking() {
        return closingBalanceInBanking;
    }

    public void setClosingBalanceInBanking(BigDecimal closingBalanceInBanking) {
        this.closingBalanceInBanking = closingBalanceInBanking;
    }

    public BigDecimal getActualCashCounted() {
        return actualCashCounted;
    }

    public void setActualCashCounted(BigDecimal actualCashCounted) {
        this.actualCashCounted = actualCashCounted;
    }

    public BigDecimal getCashDiscrepancy() {
        return cashDiscrepancy;
    }

    public void setCashDiscrepancy(BigDecimal cashDiscrepancy) {
        this.cashDiscrepancy = cashDiscrepancy;
    }

    public String getNoteDiscrepancy() {
        return noteDiscrepancy;
    }

    public void setNoteDiscrepancy(String noteDiscrepancy) {
        this.noteDiscrepancy = noteDiscrepancy;
    }

    public Account getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(Account employeeID) {
        this.employeeID = employeeID;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Status getStatusID() {
        return statusID;
    }

    public void setStatusID(Status statusID) {
        this.statusID = statusID;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}