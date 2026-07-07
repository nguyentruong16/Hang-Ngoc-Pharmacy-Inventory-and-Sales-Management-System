package com.example.project.dto.response;

import com.example.project.entity.Procurementplandetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcurementplandetailResponse {
    private Integer id;
    private Integer procurementID;
    private Integer productId;
    private Integer requestedQuantity;
    private String unit;
    private BigDecimal estimatedPrice;
    private String note;
    private Integer currentStock;
    private String reason;

    public static ProcurementplandetailResponse from(Procurementplandetail procurementplandetail) {
        return new ProcurementplandetailResponse(
                procurementplandetail.getId(),
                procurementplandetail.getProcurementID() != null ? procurementplandetail.getProcurementID().getId() : null,
                procurementplandetail.getProductID() != null ? procurementplandetail.getProductID().getProductID() : null,
                procurementplandetail.getRequestedQuantity(),
                procurementplandetail.getUnit(),
                procurementplandetail.getEstimatedPrice(),
                procurementplandetail.getNote(),
                procurementplandetail.getCurrentStock(),
                procurementplandetail.getReason()
        );
    }
}