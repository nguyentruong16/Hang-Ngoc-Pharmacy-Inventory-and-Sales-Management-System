package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "purchaserequisitiondetail")
public class Purchaserequisitiondetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "requisitionDetailID", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requisitionID", nullable = false)
    private Purchaserequisition requisitionID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "productID", nullable = false)
    private Product productID;

    @NotNull
    @Column(name = "requestedQuantity", nullable = false)
    private Integer requestedQuantity;

    @Size(max = 20)
    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "currentStock")
    private Integer currentStock;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
