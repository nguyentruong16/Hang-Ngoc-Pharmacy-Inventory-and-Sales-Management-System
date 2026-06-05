package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "income")
public class Income {
    @Id
    @Column(name = "incomeID", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branchID", nullable = false)
    private Branch branchID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicantID", nullable = false)
    private Account applicantID;

    @NotNull
    @Column(name = "date", nullable = false)
    private Instant date;

    @NotNull
    @Lob
    @Column(name = "reason", nullable = false)
    private String reason;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "paidByCash", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidByCash;

    @NotNull
    @Column(name = "paidByBanking", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidByBanking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusID")
    private Status statusID;

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

    public Account getApplicantID() {
        return applicantID;
    }

    public void setApplicantID(Account applicantID) {
        this.applicantID = applicantID;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPaidByCash() {
        return paidByCash;
    }

    public void setPaidByCash(BigDecimal paidByCash) {
        this.paidByCash = paidByCash;
    }

    public BigDecimal getPaidByBanking() {
        return paidByBanking;
    }

    public void setPaidByBanking(BigDecimal paidByBanking) {
        this.paidByBanking = paidByBanking;
    }

    public Status getStatusID() {
        return statusID;
    }

    public void setStatusID(Status statusID) {
        this.statusID = statusID;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}