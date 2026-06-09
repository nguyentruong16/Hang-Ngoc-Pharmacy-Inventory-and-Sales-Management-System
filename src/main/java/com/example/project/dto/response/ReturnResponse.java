package com.example.project.dto.response;

import com.example.project.entity.Return;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {
    private Integer id;
    private Integer invoiceId;
    private Integer branchId;
    private Integer returnedById;
    private Instant returnDate;
    private String returnType;
    private BigDecimal refundCash;
    private BigDecimal refundBanking;
    private BigDecimal refundCredit;
    private BigDecimal totalRefund;
    private BigDecimal vatRefundAmount;
    private BigDecimal offsetDebtAmount;
    private Integer offsetDebtId;
    private Integer expenseId;
    private Integer shiftReportId;
    private Integer vatAdjustmentId;
    private String reason;
    private Integer statusId;
    private Integer approvedById;
    private Instant approvedAt;
    private String note;

    public static ReturnResponse from(Return returnEntity) {
        return new ReturnResponse(
                returnEntity.getId(),
                returnEntity.getInvoiceID() != null ? returnEntity.getInvoiceID().getId() : null,
                returnEntity.getBranchID() != null ? returnEntity.getBranchID().getId() : null,
                returnEntity.getReturnedBy() != null ? returnEntity.getReturnedBy().getId() : null,
                returnEntity.getReturnDate(),
                returnEntity.getReturnType(),
                returnEntity.getRefundCash(),
                returnEntity.getRefundBanking(),
                returnEntity.getRefundCredit(),
                returnEntity.getTotalRefund(),
                returnEntity.getVatRefundAmount(),
                returnEntity.getOffsetDebtAmount(),
                returnEntity.getOffsetDebtID() != null ? returnEntity.getOffsetDebtID().getId() : null,
                returnEntity.getExpenseID() != null ? returnEntity.getExpenseID().getId() : null,
                returnEntity.getShiftReportID() != null ? returnEntity.getShiftReportID().getId() : null,
                returnEntity.getVatAdjustmentID() != null ? returnEntity.getVatAdjustmentID().getId() : null,
                returnEntity.getReason(),
                returnEntity.getStatusID() != null ? returnEntity.getStatusID().getId() : null,
                returnEntity.getApprovedBy() != null ? returnEntity.getApprovedBy().getId() : null,
                returnEntity.getApprovedAt(),
                returnEntity.getNote()
        );
    }
}
