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
@Table(name = "procurementplan")
public class Procurementplan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planID", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "procurementPlanCode", nullable = false, length = 50)
    private String procurementPlanCode;

    @NotNull
    @Column(name = "date", nullable = false)
    private Instant date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeID")
    private Account employeeID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusID")
    private Status statusID;

    @Column(name = "approveAt")
    private Instant approveAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
