package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** One conversion unit + sell price of a product, for the Product Detail screen. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitDetailResponse {
    private String unitName;
    private BigDecimal ratio;
    private BigDecimal sellPrice;
    private boolean isDefault;
    private boolean isBaseUnit;
    private boolean isActive;
}
