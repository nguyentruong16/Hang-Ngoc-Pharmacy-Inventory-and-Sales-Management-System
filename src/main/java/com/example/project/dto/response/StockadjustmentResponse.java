package com.example.project.dto.response;

import com.example.project.entity.Stockadjustment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockadjustmentResponse {
    private Integer id;
    private String stockAdjustmentCode ;
    private String adjustmentType ;
    private Instant date;
    private Integer createdById;
    private Integer approvedById;
    private Instant approvedAt;
    private String reason;
    private Integer stockCountID;
    private Integer expenseId;
    private String status;
    private String note;

    public static StockadjustmentResponse from(Stockadjustment stockadjustment) {
        return new StockadjustmentResponse(
                stockadjustment.getId(),
                stockadjustment.getStockAdjustmentCode(),
                stockadjustment.getAdjustmentType(),
                stockadjustment.getDate(),
                stockadjustment.getCreatedBy() != null ? stockadjustment.getCreatedBy().getId() : null,
                stockadjustment.getApprovedBy() != null ? stockadjustment.getApprovedBy().getId() : null,
                stockadjustment.getApprovedAt(),
                stockadjustment.getReason(),
                stockadjustment.getStockCountID() != null ? stockadjustment.getStockCountID().getId() : null,
                stockadjustment.getExpenseID() != null ? stockadjustment.getExpenseID().getId() : null,
                stockadjustment.getStatus(),
                stockadjustment.getNote()
        );
    }
}