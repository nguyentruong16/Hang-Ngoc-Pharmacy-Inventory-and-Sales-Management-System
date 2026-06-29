package com.example.project.dto.response;

import com.example.project.entity.Batch;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse {
    private Integer id;
    private String batchName;
    private Integer productId;
    private Integer purchaseDetailId;
    private Integer branchId;
    private Integer storageQuantity;
    private Integer importUnitId;
    private Integer importQtyInUnit;
    private BigDecimal importPrice;
    private BigDecimal importPricePerBase;
    private Instant importDate;
    private LocalDate productionDate;
    private LocalDate expirationDate;
    private String lotNumber;
    private Boolean status;
    private String note;

    public static BatchResponse from(Batch batch) {
        return new BatchResponse(
                batch.getId(),
                batch.getBatchName(), 
                batch.getProductID() != null ? batch.getProductID().getProductID() : null,
                batch.getPurchaseDetailID() != null ? batch.getPurchaseDetailID().getId() : null,
                batch.getBranchID() != null ? batch.getBranchID().getId() : null,
                batch.getStorageQuantity(),
                batch.getImportUnitID() != null ? batch.getImportUnitID().getId() : null,
                batch.getImportQtyInUnit(),
                batch.getImportPrice(),
                batch.getImportPricePerBase(),
                batch.getImportDate(),
                batch.getProductionDate(),
                batch.getExpirationDate(),
                batch.getLotNumber(),
                batch.getStatus(),
                batch.getNote()
        );
    }
}