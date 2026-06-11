package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "vatinvoiceconfig")
public class Vatinvoiceconfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "configID", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branchID", nullable = false)
    private Branch branchID;

    @Size(max = 10)
    @NotNull
    @Column(name = "series", nullable = false, length = 10)
    private String series;

    @Size(max = 50)
    @NotNull
    @Column(name = "pattern", nullable = false, length = 50)
    private String pattern;

    @ColumnDefault("1")
    @Column(name = "currentNumber")
    private Integer currentNumber;

    @ColumnDefault("'YEARLY'")
    @Column(name = "resetCycle", columnDefinition = "enum('YEARLY','MONTHLY','NEVER')")
    private String resetCycle;


}
