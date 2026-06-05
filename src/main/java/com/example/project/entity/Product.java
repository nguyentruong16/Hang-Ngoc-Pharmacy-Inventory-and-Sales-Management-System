package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @Size(max = 50)
    @Column(name = "productID", nullable = false, length = 50)
    private String productID;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 50)
    @NotNull
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "typeID")
    private Type typeID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "positionID")
    private Position positionID;

    @ColumnDefault("0")
    @Column(name = "maxStock")
    private Integer maxStock;

    @ColumnDefault("0")
    @Column(name = "minStock")
    private Integer minStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producerID")
    private Producer producerID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "originID")
    private Origin originID;

    @Size(max = 100)
    @Column(name = "registrationNumber", length = 100)
    private String registrationNumber;

    @Size(max = 50)
    @Column(name = "productBarcode", length = 50)
    private String productBarcode;

    @Column(name = "totalSellPrice", precision = 15, scale = 2)
    private BigDecimal totalSellPrice;

    @ColumnDefault("1")
    @Column(name = "status")
    private Boolean status;

    @Lob
    @Column(name = "note")
    private String note;

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Type getTypeID() {
        return typeID;
    }

    public void setTypeID(Type typeID) {
        this.typeID = typeID;
    }

    public Position getPositionID() {
        return positionID;
    }

    public void setPositionID(Position positionID) {
        this.positionID = positionID;
    }

    public Integer getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(Integer maxStock) {
        this.maxStock = maxStock;
    }

    public Integer getMinStock() {
        return minStock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
    }

    public Producer getProducerID() {
        return producerID;
    }

    public void setProducerID(Producer producerID) {
        this.producerID = producerID;
    }

    public Origin getOriginID() {
        return originID;
    }

    public void setOriginID(Origin originID) {
        this.originID = originID;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getProductBarcode() {
        return productBarcode;
    }

    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
    }

    public BigDecimal getTotalSellPrice() {
        return totalSellPrice;
    }

    public void setTotalSellPrice(BigDecimal totalSellPrice) {
        this.totalSellPrice = totalSellPrice;
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