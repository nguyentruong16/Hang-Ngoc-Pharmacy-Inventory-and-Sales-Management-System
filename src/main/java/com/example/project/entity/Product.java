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

    @Size(max = 50)
    @Column(name = "barcode", length = 50)
    private String barcode;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baseUnitID")
    private Productunit baseUnitID;

    @ColumnDefault("0")
    @Column(name = "isPrescription")
    private Boolean isPrescription;

    @ColumnDefault("0")
    @Column(name = "hasSerial")
    private Boolean hasSerial;

    @ColumnDefault("1")
    @Column(name = "status")
    private Boolean status;

    @Size(max = 255)
    @Column(name = "image")
    private String image;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
