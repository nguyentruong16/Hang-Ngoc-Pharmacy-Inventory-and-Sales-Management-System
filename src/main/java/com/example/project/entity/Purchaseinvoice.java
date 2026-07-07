package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "purchaseinvoice")
public class Purchaseinvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchaseID", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "purchaseInvoiceCode", nullable = false, length = 50)
    private String purchaseInvoiceCode;

    @NotNull
    @Column(name = "date", nullable = false)
    private Instant date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplierID")
    private Supplier supplierID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeID")
    private Account employeeID;

    @Column(name = "additionCost", precision = 15, scale = 2)
    private BigDecimal additionCost;

    @Column(name = "discount", precision = 15, scale = 2)
    private BigDecimal discount;

    @Column(name = "totalAmount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisitionID")
    private Purchaserequisition requisitionID;

    @Column(name = "paid", precision = 15, scale = 2)
    private BigDecimal paid;

    @Size(max = 50)
    @NotNull
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
