package com.example.project.dto.response;

import com.example.project.entity.Purchasedetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchasedetailResponse {
    private Integer id;
    private Integer purchaseId;
    private Integer productId;
    private Integer quantity;
    private BigDecimal importPrice;
    private LocalDate productionDate;
    private LocalDate expirationDate;
    private String lotNumber;

    public static PurchasedetailResponse from(Purchasedetail purchasedetail) {
        return new PurchasedetailResponse(
                purchasedetail.getId(),
                purchasedetail.getPurchaseID() != null ? purchasedetail.getPurchaseID().getId() : null,
                purchasedetail.getProductID() != null ? purchasedetail.getProductID().getProductID() : null,
                purchasedetail.getQuantity(),
                purchasedetail.getImportPrice(),
                purchasedetail.getProductionDate(),
                purchasedetail.getExpirationDate(),
                purchasedetail.getLotNumber()
        );
    }
}