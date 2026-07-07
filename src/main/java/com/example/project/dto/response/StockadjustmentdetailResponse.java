package com.example.project.dto.response;

import com.example.project.entity.Stockadjustmentdetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockadjustmentdetailResponse {
    private Integer id;
    private Integer stockAdjustmentID ;
    private Integer productId;
    private Integer productUnitId;
    private Integer batchId;
    private Integer quantity;
    private Integer baseQtyDeducted;
    private BigDecimal unitCostPrice;
    private BigDecimal lineCost;
    private String note;

    public static StockadjustmentdetailResponse from(Stockadjustmentdetail stockadjustmentdetail) {
        return new StockadjustmentdetailResponse(
                stockadjustmentdetail.getId(),
                stockadjustmentdetail.getStockAdjustmentID() != null ? stockadjustmentdetail.getStockAdjustmentID().getId() : null,
                stockadjustmentdetail.getProductID() != null ? stockadjustmentdetail.getProductID().getProductID() : null,
                stockadjustmentdetail.getProductUnitID() != null ? stockadjustmentdetail.getProductUnitID().getId() : null,
                stockadjustmentdetail.getBatchID() != null ? stockadjustmentdetail.getBatchID().getId() : null,
                stockadjustmentdetail.getQuantity(),
                stockadjustmentdetail.getBaseQtyDeducted(),
                stockadjustmentdetail.getUnitCostPrice(),
                stockadjustmentdetail.getLineCost(),
                stockadjustmentdetail.getNote()
        );
    }
}