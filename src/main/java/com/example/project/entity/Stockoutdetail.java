package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "stockoutdetail")
public class Stockoutdetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stockOutDetailID", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stockOutID", nullable = false)
    private Stockout stockOutID;

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

    @NotNull
    @Column(name = "baseQtyDeducted", nullable = false)
    private Integer baseQtyDeducted;

    @NotNull
    @Column(name = "unitCostPrice", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitCostPrice;

    @NotNull
    @Column(name = "lineCost", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineCost;

    @Lob
    @Column(name = "note")
    private String note;


}