package com.example.project.dto.response;

import com.example.project.entity.Purchaserequisition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaserequisitionResponse {
    private Integer id;
    private String purchaseRequistionCode;
    private Instant date;
    private Integer branchId;
    private Integer requestedById;
    private Integer statusId;
    private Instant approvedAt;
    private Integer supplierId;
    private String note;
    private Instant createdAt;

    public static PurchaserequisitionResponse from(Purchaserequisition purchaserequisition) {
        return new PurchaserequisitionResponse(
                purchaserequisition.getId(),
                purchaserequisition.getPurchaseRequistionCode(),
                purchaserequisition.getDate(),
                purchaserequisition.getBranchID() != null ? purchaserequisition.getBranchID().getId() : null,
                purchaserequisition.getRequestedBy() != null ? purchaserequisition.getRequestedBy().getId() : null,
                purchaserequisition.getStatusID() != null ? purchaserequisition.getStatusID().getId() : null,
                purchaserequisition.getApprovedAt(),
                purchaserequisition.getSupplierID() != null ? purchaserequisition.getSupplierID().getId() : null,
                purchaserequisition.getNote(),
                purchaserequisition.getCreatedAt()
        );
    }
}