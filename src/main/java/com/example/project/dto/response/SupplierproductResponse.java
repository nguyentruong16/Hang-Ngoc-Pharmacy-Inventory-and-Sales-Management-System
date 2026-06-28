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
    private Integer productId;
    private String productCode;
    private String productName;
    private BigDecimal costPrice;
    private Boolean isPreferred;
    private Boolean isActive;
    private String note;

    public static SupplierproductResponse from(Supplierproduct sp) {
        return new SupplierproductResponse(
                sp.getId(),
                sp.getSupplierID() != null ? sp.getSupplierID().getId() : null,
                sp.getProductID() != null ? sp.getProductID().getProductID() : null,
                sp.getProductID() != null ? sp.getProductID().getCode() : null,
                sp.getProductID() != null ? sp.getProductID().getName() : null,
                sp.getCostPrice(),
                sp.getIsPreferred(),
                sp.getIsActive(),
                sp.getNote()
        );
    }
}
