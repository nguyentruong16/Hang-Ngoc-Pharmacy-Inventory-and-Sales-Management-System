package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "productwarranty")
public class Productwarranty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warrantyID", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "warrantyCode", nullable = false, length = 50)
    private String warrantyCode;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoiceID")
    private Invoice invoiceID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productID")
    private Product productID;

    @Size(max = 50)
    @Column(name = "serialNumber", length = 50)
    private String serialNumber;

    @Column(name = "warrantyStartDate", nullable = false)
    private LocalDate warrantyStartDate;

    @Column(name = "warrantyEndDate", nullable = false)
    private LocalDate warrantyEndDate;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
