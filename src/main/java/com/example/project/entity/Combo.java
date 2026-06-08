package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "combo")
public class Combo {
    @Id
    @Size(max = 50)
    @Column(name = "comboID", nullable = false, length = 50)
    private String comboID;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comboID", nullable = false)
    private Product product;


}