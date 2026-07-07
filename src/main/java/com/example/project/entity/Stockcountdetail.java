package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "stockcountdetail")
public class Stockcountdetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stockCountDetailID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockCountID")
    private Stockcount stockCountID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productID")
    private Product productID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batchID")
    private Batch batchID;

    @NotNull
    @Column(name = "systemQty", nullable = false)
    private Integer systemQty;

    @NotNull
    @Column(name = "actualQty", nullable = false)
    private Integer actualQty;

    @Column(name = "discrepancy")
    private Integer discrepancy;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
