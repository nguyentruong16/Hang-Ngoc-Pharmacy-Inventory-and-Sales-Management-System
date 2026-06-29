package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "shiftreport")
public class Shiftreport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ColumnDefault("0")
    @Column(name = "totalReturns")
    private Integer totalReturns;

    @ColumnDefault("0.00")
    @Column(name = "totalReturnAmount", precision = 15, scale = 2)
    private BigDecimal totalReturnAmount;

    @ColumnDefault("0.00")
    @Column(name = "totalDebtCollected", precision = 15, scale = 2)
    private BigDecimal totalDebtCollected;

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

    @Column(name = "noteDiscrepancy", columnDefinition = "TEXT")
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

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;


}
