package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/** One sellable unit of a product, embedded into the create-invoice page for the inline JS. */
@Getter
@AllArgsConstructor
public class SellUnitOptionResponse {

    private Integer id;
    private String unitName;
    private BigDecimal ratio;
    private BigDecimal sellPrice;
    private boolean defaultUnit;
}
