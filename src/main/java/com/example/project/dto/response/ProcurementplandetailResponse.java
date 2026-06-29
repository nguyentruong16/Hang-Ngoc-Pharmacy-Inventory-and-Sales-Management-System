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
    private Integer planId;
    private Integer productId;
    private Integer quantity;
    private String unit;
    private BigDecimal estimatedPrice;
    private String note;

    public static ProcurementplandetailResponse from(Procurementplandetail procurementplandetail) {
        return new ProcurementplandetailResponse(
                procurementplandetail.getId(),
                procurementplandetail.getPlanID() != null ? procurementplandetail.getPlanID().getId() : null,
                procurementplandetail.getProductID() != null ? procurementplandetail.getProductID().getProductID() : null,
                procurementplandetail.getQuantity(),
                procurementplandetail.getUnit(),
                procurementplandetail.getEstimatedPrice(),
                procurementplandetail.getNote()
        );
    }
}