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
public class ProcurementSupplierSearchResponse {
    private Integer supplierId;
    private String name;
    private BigDecimal lastCostPrice;
}
