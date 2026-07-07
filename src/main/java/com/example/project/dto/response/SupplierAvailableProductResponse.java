package com.example.project.dto.response;

import com.example.project.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lightweight view of a product that is NOT yet linked to a given supplier.
 * Used to populate the "Thêm sản phẩm cung ứng" picker modal on the supplier detail screen.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierAvailableProductResponse {
    private Integer productId;
    private String code;
    private String name;
    private String typeName;

    public static SupplierAvailableProductResponse from(Product p) {
        return new SupplierAvailableProductResponse(
                p.getProductID(),
                p.getCode(),
                p.getName(),
                p.getTypeID() != null ? p.getTypeID().getName() : null
        );
    }
}
