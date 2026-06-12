package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "combocomponent")
public class Combocomponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comboComponentID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comboID")
    private Combo comboID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "componentProductID")
    private Product componentProductID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "componentUnitID")
    private Productunit componentUnitID;

    @NotNull
    @Column(name = "quantity", nullable = false, precision = 10, scale = 4)
    private BigDecimal quantity;

    @NotNull
    @Column(name = "baseQtyRequired", nullable = false)
    private Integer baseQtyRequired;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
