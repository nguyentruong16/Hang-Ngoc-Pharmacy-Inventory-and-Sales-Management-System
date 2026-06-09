package com.example.project.dto.response;

import com.example.project.entity.Supplierproduct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierproductResponse {
    private Integer id;
    private Integer supplierId;
    private String productId;
    private BigDecimal costPrice;
    private Boolean isPreferred;
    private Boolean isActive;
    private String note;

    public static SupplierproductResponse from(Supplierproduct supplierproduct) {
        return new SupplierproductResponse(
                supplierproduct.getId(),
                supplierproduct.getSupplierID() != null ? supplierproduct.getSupplierID().getId() : null,
                supplierproduct.getProductID() != null ? supplierproduct.getProductID().getProductID() : null,
                supplierproduct.getCostPrice(),
                supplierproduct.getIsPreferred(),
                supplierproduct.getIsActive(),
                supplierproduct.getNote()
        );
    }
}