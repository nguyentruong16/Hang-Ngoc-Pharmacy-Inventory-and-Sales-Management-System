package com.example.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "accountpermission")
public class Accountpermission {
    @Id
    @Column(name = "accountPermissionID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountID")
    private Account accountID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branchID")
    private Branch branchID;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Account getAccountID() {
        return accountID;
    }

    public void setAccountID(Account accountID) {
        this.accountID = accountID;
    }

    public Branch getBranchID() {
        return branchID;
    }

    public void setBranchID(Branch branchID) {
        this.branchID = branchID;
    }

}