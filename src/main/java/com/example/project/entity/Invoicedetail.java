package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "invoicedetail")
public class Invoicedetail {
    @Id
    @Column(name = "invoiceDetailID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoiceID")
    private Invoice invoiceID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productID")
    private Product productID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batchID")
    private Batch batchID;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Size(max = 20)
    @Column(name = "unit", length = 20)
    private String unit;

    @NotNull
    @Column(name = "unitSellPrice", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitSellPrice;

    @NotNull
    @Column(name = "unitImportPrice", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitImportPrice;

    @NotNull
    @Column(name = "subimport", nullable = false, precision = 15, scale = 2)
    private BigDecimal subimport;

    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Invoice getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(Invoice invoiceID) {
        this.invoiceID = invoiceID;
    }

    public Product getProductID() {
        return productID;
    }

    public void setProductID(Product productID) {
        this.productID = productID;
    }

    public Batch getBatchID() {
        return batchID;
    }

    public void setBatchID(Batch batchID) {
        this.batchID = batchID;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitSellPrice() {
        return unitSellPrice;
    }

    public void setUnitSellPrice(BigDecimal unitSellPrice) {
        this.unitSellPrice = unitSellPrice;
    }

    public BigDecimal getUnitImportPrice() {
        return unitImportPrice;
    }

    public void setUnitImportPrice(BigDecimal unitImportPrice) {
        this.unitImportPrice = unitImportPrice;
    }

    public BigDecimal getSubimport() {
        return subimport;
    }

    public void setSubimport(BigDecimal subimport) {
        this.subimport = subimport;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

}