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
@Table(name = "debt")
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "debtID", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "debtType", nullable = false, columnDefinition = "enum('CUSTOMER','SUPPLIER')")
    private String debtType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerID")
    private Customer customerID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplierID")
    private Supplier supplierID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoiceID")
    private Invoice invoiceID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaseInvoiceID")
    private Purchaseinvoice purchaseInvoiceID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returnID")
    private Return returnID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branchID", nullable = false)
    private Branch branchID;

    @NotNull
    @Column(name = "originalAmount", nullable = false, precision = 15, scale = 2)
    private BigDecimal originalAmount;

    @ColumnDefault("0.00")
    @Column(name = "paidAmount", precision = 15, scale = 2)
    private BigDecimal paidAmount;

    @NotNull
    @Column(name = "remainingAmount", nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "dueDate")
    private LocalDate dueDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statusID", nullable = false)
    private Status statusID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "createdBy", nullable = false)
    private Account createdBy;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
