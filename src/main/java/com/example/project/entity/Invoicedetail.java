package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "invoicedetail")
public class Invoicedetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoiceDetailID", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoiceID", nullable = false)
    private Invoice invoiceID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "productID", nullable = false)
    private Product productID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "productUnitID", nullable = false)
    private Productunit productUnitID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batchID", nullable = false)
    private Batch batchID;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Size(max = 20)
    @NotNull
    @Column(name = "unitName", nullable = false, length = 20)
    private String unitName;

    @NotNull
    @Column(name = "baseQtyDeducted", nullable = false)
    private Integer baseQtyDeducted;

    @NotNull
    @Column(name = "unitSellPrice", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitSellPrice;

    @NotNull
    @Column(name = "unitImportPrice", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitImportPrice;

    @ColumnDefault("0.00")
    @Column(name = "vatRate", precision = 5, scale = 2)
    private BigDecimal vatRate;

    @ColumnDefault("0.00")
    @Column(name = "vatAmount", precision = 15, scale = 2)
    private BigDecimal vatAmount;

    @NotNull
    @Column(name = "subimport", nullable = false, precision = 15, scale = 2)
    private BigDecimal subimport;

    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @Column(name = "lineTotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineTotal;

    @ColumnDefault("0")
    @Column(name = "returnedQty")
    private Integer returnedQty;


}