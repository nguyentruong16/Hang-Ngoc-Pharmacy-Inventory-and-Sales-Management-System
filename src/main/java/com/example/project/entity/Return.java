package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "`return`")
public class Return {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "returnID", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoiceID", nullable = false)
    private Invoice invoiceID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branchID", nullable = false)
    private Branch branchID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "returnedBy", nullable = false)
    private Account returnedBy;

    @NotNull
    @Column(name = "returnDate", nullable = false)
    private Instant returnDate;

    @NotNull
    @Lob
    @Column(name = "returnType", nullable = false)
    private String returnType;

    @ColumnDefault("0.00")
    @Column(name = "refundCash", precision = 15, scale = 2)
    private BigDecimal refundCash;

    @ColumnDefault("0.00")
    @Column(name = "refundBanking", precision = 15, scale = 2)
    private BigDecimal refundBanking;

    @ColumnDefault("0.00")
    @Column(name = "refundCredit", precision = 15, scale = 2)
    private BigDecimal refundCredit;

    @NotNull
    @Column(name = "totalRefund", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRefund;

    @ColumnDefault("0.00")
    @Column(name = "vatRefundAmount", precision = 15, scale = 2)
    private BigDecimal vatRefundAmount;

    @ColumnDefault("0.00")
    @Column(name = "offsetDebtAmount", precision = 15, scale = 2)
    private BigDecimal offsetDebtAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offsetDebtID")
    private Debt offsetDebtID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expenseID")
    private Expense expenseID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shiftReportID")
    private Shiftreport shiftReportID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vatAdjustmentID")
    private Vatinvoice vatAdjustmentID;

    @NotNull
    @Lob
    @Column(name = "reason", nullable = false)
    private String reason;

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


}