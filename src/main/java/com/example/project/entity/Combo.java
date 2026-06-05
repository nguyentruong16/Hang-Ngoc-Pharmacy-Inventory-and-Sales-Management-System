package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "combo")
public class Combo {
    @Id
    @Size(max = 50)
    @Column(name = "comboNum", nullable = false, length = 50)
    private String comboNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comboID")
    private Product comboID;

    public String getComboNum() {
        return comboNum;
    }

    public void setComboNum(String comboNum) {
        this.comboNum = comboNum;
    }

    public Product getComboID() {
        return comboID;
    }

    public void setComboID(Product comboID) {
        this.comboID = comboID;
    }

}