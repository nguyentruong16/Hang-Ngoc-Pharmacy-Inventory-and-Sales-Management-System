package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerID", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "customerType", nullable = false, columnDefinition = "enum('INDIVIDUAL','COMPANY')")
    private String customerType;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 100)
    @Column(name = "taxCode", length = 100)
    private String taxCode;

    @Size(max = 100)
    @Column(name = "address", length = 100)
    private String address;

    @Size(max = 100)
    @Column(name = "bankAccountNumber", length = 100)
    private String bankAccountNumber;

    @Size(max = 100)
    @Column(name = "bankName", length = 100)
    private String bankName;

    @Size(max = 20)
    @Column(name = "phoneNumber", length = 20)
    private String phoneNumber;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
