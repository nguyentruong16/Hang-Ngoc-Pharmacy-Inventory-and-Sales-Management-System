package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "combocomponent")
public class Combocomponent {
    @Id
    @Column(name = "comboComponentID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comboID", referencedColumnName = "comboID")
    private Combo comboID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "componentID")
    private Product componentID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batchID")
    private Batch batchID;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Size(max = 20)
    @Column(name = "unitName", length = 20)
    private String unitName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Combo getComboID() {
        return comboID;
    }

    public void setComboID(Combo comboID) {
        this.comboID = comboID;
    }

    public Product getComponentID() {
        return componentID;
    }

    public void setComponentID(Product componentID) {
        this.componentID = componentID;
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

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

}