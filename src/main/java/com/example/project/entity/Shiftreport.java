package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "shiftreport")
public class Shiftreport {
    @Id
    @Column(name = "shiftReportID", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branchID", nullable = false)
    private Branch branchID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cashierID", nullable = false)
    private Account cashierID;

    @NotNull
    @Column(name = "shiftDate", nullable = false)
    private LocalDate shiftDate;

    @Size(max = 20)
    @NotNull
    @Column(name = "shiftType", nullable = false, length = 20)
    private String shiftType;

    @NotNull
    @Column(name = "startTime", nullable = false)
    private Instant startTime;

    @Column(name = "endTime")
    private Instant endTime;

    @NotNull
    @Column(name = "openingCash", nullable = false, precision = 15, scale = 2)
    private BigDecimal openingCash;

    @ColumnDefault("0")
    @Column(name = "totalInvoices")
    private Integer totalInvoices;

    @ColumnDefault("0.00")
    @Column(name = "totalRevenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @ColumnDefault("0.00")
    @Column(name = "totalCashIn", precision = 15, scale = 2)
    private BigDecimal totalCashIn;

    @ColumnDefault("0.00")
    @Column(name = "totalBankingIn", precision = 15, scale = 2)
    private BigDecimal totalBankingIn;

    @ColumnDefault("0.00")
    @Column(name = "totalCashOut", precision = 15, scale = 2)
    private BigDecimal totalCashOut;

    @Column(name = "expectedClosingCash", precision = 15, scale = 2)
    private BigDecimal expectedClosingCash;

    @Column(name = "actualClosingCash", precision = 15, scale = 2)
    private BigDecimal actualClosingCash;

    @Column(name = "cashDiscrepancy", precision = 15, scale = 2)
    private BigDecimal cashDiscrepancy;

    @Lob
    @Column(name = "noteDiscrepancy")
    private String noteDiscrepancy;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statusID", nullable = false)
    private Status statusID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedBy")
    private Account approvedBy;

    @Column(name = "approvedAt")
    private Instant approvedAt;

    @Lob
    @Column(name = "note")
    private String note;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

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

    public Account getCashierID() {
        return cashierID;
    }

    public void setCashierID(Account cashierID) {
        this.cashierID = cashierID;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getShiftType() {
        return shiftType;
    }

    public void setShiftType(String shiftType) {
        this.shiftType = shiftType;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getOpeningCash() {
        return openingCash;
    }

    public void setOpeningCash(BigDecimal openingCash) {
        this.openingCash = openingCash;
    }

    public Integer getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(Integer totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalCashIn() {
        return totalCashIn;
    }

    public void setTotalCashIn(BigDecimal totalCashIn) {
        this.totalCashIn = totalCashIn;
    }

    public BigDecimal getTotalBankingIn() {
        return totalBankingIn;
    }

    public void setTotalBankingIn(BigDecimal totalBankingIn) {
        this.totalBankingIn = totalBankingIn;
    }

    public BigDecimal getTotalCashOut() {
        return totalCashOut;
    }

    public void setTotalCashOut(BigDecimal totalCashOut) {
        this.totalCashOut = totalCashOut;
    }

    public BigDecimal getExpectedClosingCash() {
        return expectedClosingCash;
    }

    public void setExpectedClosingCash(BigDecimal expectedClosingCash) {
        this.expectedClosingCash = expectedClosingCash;
    }

    public BigDecimal getActualClosingCash() {
        return actualClosingCash;
    }

    public void setActualClosingCash(BigDecimal actualClosingCash) {
        this.actualClosingCash = actualClosingCash;
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

    public Status getStatusID() {
        return statusID;
    }

    public void setStatusID(Status statusID) {
        this.statusID = statusID;
    }

    public Account getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Account approvedBy) {
        this.approvedBy = approvedBy;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}