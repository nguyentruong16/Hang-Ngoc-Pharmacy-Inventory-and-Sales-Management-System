package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "income")
public class Income {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Column(name = "incomeType", nullable = false, columnDefinition = "enum('SUPPLIER','EMPLOYEE','CUSTOMER','OTHER')")
    private String incomeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoiceID")
    private Invoice invoiceID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returnID")
    private Return returnID;

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
    @Column(name = "paidByCash", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidByCash;

    @NotNull
    @Column(name = "paidByBanking", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidByBanking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplierID")
    private Supplier supplierID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerID")
    private Customer customerID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountID")
    private Account accountID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusID")
    private Status statusID;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
