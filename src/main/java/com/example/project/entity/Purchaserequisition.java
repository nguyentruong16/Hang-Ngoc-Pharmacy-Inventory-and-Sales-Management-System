package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "purchaserequisition")
public class Purchaserequisition {
    @Id
    @Column(name = "requisitionID", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "date", nullable = false)
    private Instant date;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branchID", nullable = false)
    private Branch branchID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requestedBy", nullable = false)
    private Account requestedBy;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statusID", nullable = false)
    private Status statusID;

    @Column(name = "approvedAt")
    private Instant approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planID")
    private Procurementplan planID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplierID", nullable = false)
    private Supplier supplierID;

    @Lob
    @Column(name = "note")
    private String note;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

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

    public Account getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Account requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Status getStatusID() {
        return statusID;
    }

    public void setStatusID(Status statusID) {
        this.statusID = statusID;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Procurementplan getPlanID() {
        return planID;
    }

    public void setPlanID(Procurementplan planID) {
        this.planID = planID;
    }

    public Supplier getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(Supplier supplierID) {
        this.supplierID = supplierID;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}