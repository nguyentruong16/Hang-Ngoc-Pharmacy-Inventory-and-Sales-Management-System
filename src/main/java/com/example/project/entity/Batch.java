package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "batch")
public class Batch {
    @Id
    @Column(name = "batchID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productID")
    private Product productID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaseDetailID")
    private Purchasedetail purchaseDetailID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branchID")
    private Branch branchID;

    @NotNull
    @Column(name = "storageQuantity", nullable = false)
    private Integer storageQuantity;

    @NotNull
    @Column(name = "importPrice", nullable = false, precision = 15, scale = 2)
    private BigDecimal importPrice;

    @NotNull
    @Column(name = "importDate", nullable = false)
    private Instant importDate;

    @Column(name = "productionDate")
    private LocalDate productionDate;

    @Column(name = "expirationDate")
    private LocalDate expirationDate;

    @Size(max = 50)
    @Column(name = "lotNumber", length = 50)
    private String lotNumber;

    @Column(name = "status")
    private Boolean status;

    @Lob
    @Column(name = "note")
    private String note;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Product getProductID() {
        return productID;
    }

    public void setProductID(Product productID) {
        this.productID = productID;
    }

    public Purchasedetail getPurchaseDetailID() {
        return purchaseDetailID;
    }

    public void setPurchaseDetailID(Purchasedetail purchaseDetailID) {
        this.purchaseDetailID = purchaseDetailID;
    }

    public Branch getBranchID() {
        return branchID;
    }

    public void setBranchID(Branch branchID) {
        this.branchID = branchID;
    }

    public Integer getStorageQuantity() {
        return storageQuantity;
    }

    public void setStorageQuantity(Integer storageQuantity) {
        this.storageQuantity = storageQuantity;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public Instant getImportDate() {
        return importDate;
    }

    public void setImportDate(Instant importDate) {
        this.importDate = importDate;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}