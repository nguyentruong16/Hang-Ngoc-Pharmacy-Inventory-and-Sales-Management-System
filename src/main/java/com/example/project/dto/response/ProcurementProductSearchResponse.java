package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcurementProductSearchResponse {
    private Integer productID;
    private String name;
    private String code;
    private String barcode;
    private Integer currentStock;
    private String stockUnit;
    private String unit;
    private BigDecimal estimatedPrice;
}
