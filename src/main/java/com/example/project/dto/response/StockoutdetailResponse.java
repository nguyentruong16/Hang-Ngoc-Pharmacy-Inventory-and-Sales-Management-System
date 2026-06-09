package com.example.project.dto.response;

import com.example.project.entity.Stockoutdetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockoutdetailResponse {
    private Integer id;
    private Integer stockOutId;
    private String productId;
    private Integer productUnitId;
    private Integer batchId;
    private Integer quantity;
    private Integer baseQtyDeducted;
    private BigDecimal unitCostPrice;
    private BigDecimal lineCost;
    private String note;

    public static StockoutdetailResponse from(Stockoutdetail stockoutdetail) {
        return new StockoutdetailResponse(
                stockoutdetail.getId(),
                stockoutdetail.getStockOutID() != null ? stockoutdetail.getStockOutID().getId() : null,
                stockoutdetail.getProductID() != null ? stockoutdetail.getProductID().getProductID() : null,
                stockoutdetail.getProductUnitID() != null ? stockoutdetail.getProductUnitID().getId() : null,
                stockoutdetail.getBatchID() != null ? stockoutdetail.getBatchID().getId() : null,
                stockoutdetail.getQuantity(),
                stockoutdetail.getBaseQtyDeducted(),
                stockoutdetail.getUnitCostPrice(),
                stockoutdetail.getLineCost(),
                stockoutdetail.getNote()
        );
    }
}