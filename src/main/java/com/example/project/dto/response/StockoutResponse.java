package com.example.project.dto.response;

import com.example.project.entity.Stockout;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockoutResponse {
    private Integer id;
    private String outType;
    private Integer branchId;
    private Instant date;
    private Integer createdById;
    private Integer approvedById;
    private Instant approvedAt;
    private String reason;
    private Integer targetBranchId;
    private Integer expenseId;
    private Integer statusId;
    private String note;

    public static StockoutResponse from(Stockout stockout) {
        return new StockoutResponse(
                stockout.getId(),
                stockout.getOutType(),
                stockout.getBranchID() != null ? stockout.getBranchID().getId() : null,
                stockout.getDate(),
                stockout.getCreatedBy() != null ? stockout.getCreatedBy().getId() : null,
                stockout.getApprovedBy() != null ? stockout.getApprovedBy().getId() : null,
                stockout.getApprovedAt(),
                stockout.getReason(),
                stockout.getTargetBranchID() != null ? stockout.getTargetBranchID().getId() : null,
                stockout.getExpenseID() != null ? stockout.getExpenseID().getId() : null,
                stockout.getStatusID() != null ? stockout.getStatusID().getId() : null,
                stockout.getNote()
        );
    }
}