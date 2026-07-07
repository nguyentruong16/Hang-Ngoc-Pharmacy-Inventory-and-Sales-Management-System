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
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoiceID", nullable = false)
    private Integer id;

    @Size(max = 6)
    @NotNull
    @Column(name = "invoiceCode", nullable = false, length = 6)
    private String invoiceCode;

    @Size(max = 50)
    @NotNull
    @Column(name = "invoiceNumber", nullable = false, length = 50)
    private String invoiceNumber;

    @NotNull
    @Column(name = "date", nullable = false)
    private Instant date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeID")
    private Account employeeID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerID")
    private Customer customerID;

    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @Column(name = "subimport", nullable = false, precision = 15, scale = 2)
    private BigDecimal subimport;

    @ColumnDefault("0.00")
    @Column(name = "discount", precision = 15, scale = 2)
    private BigDecimal discount;

    @ColumnDefault("0.00")
    @Column(name = "taxAmount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @NotNull
    @Column(name = "total", nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @NotNull
    @Column(name = "paidByCash", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidByCash;

    @NotNull
    @Column(name = "paidByBanking", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidByBanking;

    @ColumnDefault("0.00")
    @Column(name = "debtAmount", precision = 15, scale = 2)
    private BigDecimal debtAmount;

    @ColumnDefault("0")
    @Column(name = "prescriptionRequired")
    private Boolean prescriptionRequired;

    @Size(max = 50)
    @NotNull
    @Column(name = "returnStatus", nullable = false, length = 50)
    private String returnStatus;

    @Size(max = 50)
    @NotNull
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shiftReportID")
    private Shiftreport shiftReportID;

    @Column(name = "prescriptionCode", columnDefinition = "TEXT")
    private String prescriptionCode;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
