package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "debtpayment")
public class Debtpayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentID", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "debtID", nullable = false)
    private Debt debtID;

    @NotNull
    @Column(name = "paymentDate", nullable = false)
    private Instant paymentDate;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @ColumnDefault("0.00")
    @Column(name = "paidByCash", precision = 15, scale = 2)
    private BigDecimal paidByCash;

    @ColumnDefault("0.00")
    @Column(name = "paidByBanking", precision = 15, scale = 2)
    private BigDecimal paidByBanking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incomeID")
    private Income incomeID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expenseID")
    private Expense expenseID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recordedBy", nullable = false)
    private Account recordedBy;

    @Lob
    @Column(name = "note")
    private String note;


}