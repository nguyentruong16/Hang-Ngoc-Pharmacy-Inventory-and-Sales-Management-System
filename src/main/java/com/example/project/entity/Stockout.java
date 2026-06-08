package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "stockout")
public class Stockout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stockOutID", nullable = false)
    private Integer id;

    @NotNull
    @Lob
    @Column(name = "outType", nullable = false)
    private String outType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branchID", nullable = false)
    private Branch branchID;

    @NotNull
    @Column(name = "date", nullable = false)
    private Instant date;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "createdBy", nullable = false)
    private Account createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedBy")
    private Account approvedBy;

    @Column(name = "approvedAt")
    private Instant approvedAt;

    @NotNull
    @Lob
    @Column(name = "reason", nullable = false)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "targetBranchID")
    private Branch targetBranchID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expenseID")
    private Expense expenseID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statusID", nullable = false)
    private Status statusID;

    @Lob
    @Column(name = "note")
    private String note;


}