package com.example.project.dto.response;

import com.example.project.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Integer productID;
    private String name;
    private String code;
    private String barcode;
    private Integer typeId;
    private Integer maxStock;
    private Integer minStock;
    private Integer producerId;
    private String origin;
    private String registrationNumber;
    private Boolean status;
    private String image;
    private String note;

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getProductID(),
                product.getName(),
                product.getCode(),
                product.getBarcode(),
                product.getTypeID() != null ? product.getTypeID().getId() : null,
                product.getMaxStock(),
                product.getMinStock(),
                product.getProducerID() != null ? product.getProducerID().getId() : null,
                product.getOrigin(),
                product.getRegistrationNumber(),
                product.getStatus(),
                product.getImage(),
                product.getNote()
        );
    }
}