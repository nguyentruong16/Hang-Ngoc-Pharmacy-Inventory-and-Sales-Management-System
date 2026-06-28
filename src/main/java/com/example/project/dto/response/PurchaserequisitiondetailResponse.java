package com.example.project.dto.response;

import com.example.project.entity.Purchaserequisitiondetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaserequisitiondetailResponse {
    private Integer id;
    private Integer requisitionId;
    private Integer productId;
    private Integer requestedQuantity;
    private String unit;
    private Integer currentStock;
    private String reason;
    private String note;

    public static PurchaserequisitiondetailResponse from(Purchaserequisitiondetail purchaserequisitiondetail) {
        return new PurchaserequisitiondetailResponse(
                purchaserequisitiondetail.getId(),
                purchaserequisitiondetail.getRequisitionID() != null ? purchaserequisitiondetail.getRequisitionID().getId() : null,
                purchaserequisitiondetail.getProductID() != null ? purchaserequisitiondetail.getProductID().getProductID() : null,
                purchaserequisitiondetail.getRequestedQuantity(),
                purchaserequisitiondetail.getUnit(),
                purchaserequisitiondetail.getCurrentStock(),
                purchaserequisitiondetail.getReason(),
                purchaserequisitiondetail.getNote()
        );
    }
}