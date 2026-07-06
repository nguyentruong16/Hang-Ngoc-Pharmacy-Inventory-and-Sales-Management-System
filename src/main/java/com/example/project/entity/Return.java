package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 50)
    @NotNull
    @Column(name = "returnCode", nullable = false, length = 50)
    private String returnCode;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoiceID", nullable = false)
    private Invoice invoiceID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "returnedBy", nullable = false)
    private Account returnedBy;

    @NotNull
    @Column(name = "returnDate", nullable = false)
    private Instant returnDate;

    @NotNull
    @Column(name = "returnType", nullable = false, columnDefinition = "enum('CASH','BANKING', 'DEBT')")
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
    @Column(name = "offsetDebtAmount", precision = 15, scale = 2)
    private BigDecimal offsetDebtAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expenseID")
    private Expense expenseID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shiftReportID")
    private Shiftreport shiftReportID;

    @NotNull
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statusID", nullable = false)
    private Status statusID;

    @Column(name = "approvedAt")
    private Instant approvedAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
