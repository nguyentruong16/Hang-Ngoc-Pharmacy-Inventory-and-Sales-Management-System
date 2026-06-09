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
    private String productID;
    private String name;
    private String code;
    private String internalBarcode;
    private Integer typeId;
    private Integer positionId;
    private Integer maxStock;
    private Integer minStock;
    private Integer producerId;
    private Integer originId;
    private String registrationNumber;
    private Integer baseUnitId;
    private Boolean isPrescription;
    private BigDecimal defaultVatRate;
    private Boolean status;
    private String image;
    private String note;

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getProductID(),
                product.getName(),
                product.getCode(),
                product.getInternalBarcode(),
                product.getTypeID() != null ? product.getTypeID().getId() : null,
                product.getPositionID() != null ? product.getPositionID().getId() : null,
                product.getMaxStock(),
                product.getMinStock(),
                product.getProducerID() != null ? product.getProducerID().getId() : null,
                product.getOriginID() != null ? product.getOriginID().getId() : null,
                product.getRegistrationNumber(),
                product.getBaseUnitID() != null ? product.getBaseUnitID().getId() : null,
                product.getIsPrescription(),
                product.getDefaultVatRate(),
                product.getStatus(),
                product.getImage(),
                product.getNote()
        );
    }
}