package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @Column(name = "invoiceID", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "date", nullable = false)
    private Instant date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branchID")
    private Branch branchID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeID")
    private Account employeeID;

    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @Column(name = "subimport", nullable = false, precision = 15, scale = 2)
    private BigDecimal subimport;

    @ColumnDefault("0.00")
    @Column(name = "discount", precision = 15, scale = 2)
    private BigDecimal discount;

    @ColumnDefault("0.00")
    @Column(name = "tax", precision = 15, scale = 2)
    private BigDecimal tax;

    @NotNull
    @Column(name = "total", nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @NotNull
    @Column(name = "paidByCash", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidByCash;

    @NotNull
    @Column(name = "paidByBanking", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidByBanking;

    @ColumnDefault("0")
    @Column(name = "prescriptionRequired")
    private Boolean prescriptionRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusID")
    private Status statusID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerID")
    private Customer customerID;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Branch getBranchID() {
        return branchID;
    }

    public void setBranchID(Branch branchID) {
        this.branchID = branchID;
    }

    public Account getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(Account employeeID) {
        this.employeeID = employeeID;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getSubimport() {
        return subimport;
    }

    public void setSubimport(BigDecimal subimport) {
        this.subimport = subimport;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPaidByCash() {
        return paidByCash;
    }

    public void setPaidByCash(BigDecimal paidByCash) {
        this.paidByCash = paidByCash;
    }

    public BigDecimal getPaidByBanking() {
        return paidByBanking;
    }

    public void setPaidByBanking(BigDecimal paidByBanking) {
        this.paidByBanking = paidByBanking;
    }

    public Boolean getPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(Boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public Status getStatusID() {
        return statusID;
    }

    public void setStatusID(Status statusID) {
        this.statusID = statusID;
    }

    public Customer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Customer customerID) {
        this.customerID = customerID;
    }

}