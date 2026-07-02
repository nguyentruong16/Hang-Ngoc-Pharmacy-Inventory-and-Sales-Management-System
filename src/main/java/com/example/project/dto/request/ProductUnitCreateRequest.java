package com.example.project.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One conversion-unit row of the Create Product form. Units are entered smallest → largest.
 *
 * <p>{@code quantityRelativeToPrevious} is how many of the <em>previous</em> row's unit make up
 * this unit (e.g. "1 vỉ = 10 viên" → 10). The service turns this into the cumulative
 * {@code ProductUnit.ratio} (relative to the smallest/base unit). {@code sellPrice} is the actual
 * price the Owner wants; if left blank the service falls back to the suggested price
 * (base price × ratio).</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductUnitCreateRequest {
    private String unitName;
    /** How many of the previous unit equal one of this unit. Ignored for the base (first) row. */
    private Integer quantityRelativeToPrevious;
    /** Actual sell price for this unit; blank/0 → service uses the suggested price. */
    private BigDecimal sellPrice;
    private boolean baseUnit;
    private boolean defaultUnit;
    private boolean active;
}
