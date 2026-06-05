package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Entity
@Table(name = "unitconversion")
public class Unitconversion {
    @Id
    @Column(name = "unitConversionID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productID")
    private Product productID;

    @Size(max = 20)
    @NotNull
    @Column(name = "unitName", nullable = false, length = 20)
    private String unitName;

    @NotNull
    @Column(name = "ratio", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratio;

    @NotNull
    @Column(name = "unitSellPrice", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitSellPrice;

    @ColumnDefault("0")
    @Column(name = "isDefault")
    private Boolean isDefault;

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

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    public BigDecimal getUnitSellPrice() {
        return unitSellPrice;
    }

    public void setUnitSellPrice(BigDecimal unitSellPrice) {
        this.unitSellPrice = unitSellPrice;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

}