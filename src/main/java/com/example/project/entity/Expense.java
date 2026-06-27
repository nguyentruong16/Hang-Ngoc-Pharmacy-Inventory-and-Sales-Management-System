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
@Table(name = "expense")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expenseID", nullable = false)
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
    @Column(name = "expenseType", nullable = false, columnDefinition = "enum('OPERATIONAL','DEBT_PAYMENT','RETURN_REFUND_PAYOUT','OTHER')")
    private String expenseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returnID")
    private Return returnID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaseID")
    private Purchaseinvoice purchaseID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shiftReportID")
    private Shiftreport shiftReportID;

    @NotNull
    @Column(name = "date", nullable = false)
    private Instant date;

    @NotNull
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "paid", nullable = false, precision = 15, scale = 2)
    private BigDecimal paid;

    @ColumnDefault("0.00")
    @Column(name = "paidByCash", precision = 15, scale = 2)
    private BigDecimal paidByCash;

    @ColumnDefault("0.00")
    @Column(name = "paidByBanking", precision = 15, scale = 2)
    private BigDecimal paidByBanking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusID")
    private Status statusID;

    @Column(name = "approvedAt")
    private Instant approvedAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
