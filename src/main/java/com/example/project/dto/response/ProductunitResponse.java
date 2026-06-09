package com.example.project.dto.response;

import com.example.project.entity.Productunit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductunitResponse {
    private Integer id;
    private String productId;
    private String unitName;
    private BigDecimal ratio;
    private BigDecimal sellPrice;
    private Boolean isDefault;
    private Boolean isBaseUnit;
    private Boolean isActive;
    private String note;

    public static ProductunitResponse from(Productunit productunit) {
        return new ProductunitResponse(
                productunit.getId(),
                productunit.getProductID() != null ? productunit.getProductID().getProductID() : null,
                productunit.getUnitName(),
                productunit.getRatio(),
                productunit.getSellPrice(),
                productunit.getIsDefault(),
                productunit.getIsBaseUnit(),
                productunit.getIsActive(),
                productunit.getNote()
        );
    }
}