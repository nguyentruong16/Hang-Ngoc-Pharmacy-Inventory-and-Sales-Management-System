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
@Table(name = "financialsetting")
public class Financialsetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "financialSettingID", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branchID", nullable = false)
    private Branch branchID;

    @ColumnDefault("0.00")
    @Column(name = "vatRate", precision = 5, scale = 2)
    private BigDecimal vatRate;

    @ColumnDefault("1")
    @Column(name = "taxCalculationMethod")
    private Integer taxCalculationMethod;

    @ColumnDefault("0.00")
    @Column(name = "returnProductOnInvoiceValueRate", precision = 5, scale = 2)
    private BigDecimal returnProductOnInvoiceValueRate;

    @ColumnDefault("0")
    @Column(name = "autoGenerateVATInvoice")
    private Boolean autoGenerateVATInvoice;

    @Size(max = 10)
    @Column(name = "vatInvoiceSeries", length = 10)
    private String vatInvoiceSeries;

    @ColumnDefault("0")
    @Column(name = "vatInvoiceLastNumber")
    private Integer vatInvoiceLastNumber;

    @ColumnDefault("0.00")
    @Column(name = "openingCashDefault", precision = 15, scale = 2)
    private BigDecimal openingCashDefault;


}