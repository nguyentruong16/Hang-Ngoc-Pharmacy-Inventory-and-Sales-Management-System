package com.example.project.dto.response;

import com.example.project.entity.Shiftreport;
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
public class ShiftreportResponse {
    private Integer id;
    private Integer branchId;
    private Integer cashierId;
    private LocalDate shiftDate;
    private String shiftType;
    private Instant startTime;
    private Instant endTime;
    private BigDecimal openingCash;
    private Integer totalInvoices;
    private BigDecimal totalRevenue;
    private Integer totalReturns;
    private BigDecimal totalReturnAmount;
    private BigDecimal totalDebtCollected;
    private BigDecimal totalCashIn;
    private BigDecimal totalBankingIn;
    private BigDecimal totalCashOut;
    private BigDecimal expectedClosingCash;
    private BigDecimal actualClosingCash;
    private BigDecimal cashDiscrepancy;
    private String noteDiscrepancy;
    private Integer statusId;
    private Integer approvedById;
    private Instant approvedAt;
    private String note;
    private Instant createdAt;

    public static ShiftreportResponse from(Shiftreport shiftreport) {
        return new ShiftreportResponse(
                shiftreport.getId(),
                shiftreport.getBranchID() != null ? shiftreport.getBranchID().getId() : null,
                shiftreport.getCashierID() != null ? shiftreport.getCashierID().getId() : null,
                shiftreport.getShiftDate(),
                shiftreport.getShiftType(),
                shiftreport.getStartTime(),
                shiftreport.getEndTime(),
                shiftreport.getOpeningCash(),
                shiftreport.getTotalInvoices(),
                shiftreport.getTotalRevenue(),
                shiftreport.getTotalReturns(),
                shiftreport.getTotalReturnAmount(),
                shiftreport.getTotalDebtCollected(),
                shiftreport.getTotalCashIn(),
                shiftreport.getTotalBankingIn(),
                shiftreport.getTotalCashOut(),
                shiftreport.getExpectedClosingCash(),
                shiftreport.getActualClosingCash(),
                shiftreport.getCashDiscrepancy(),
                shiftreport.getNoteDiscrepancy(),
                shiftreport.getStatusID() != null ? shiftreport.getStatusID().getId() : null,
                shiftreport.getApprovedBy() != null ? shiftreport.getApprovedBy().getId() : null,
                shiftreport.getApprovedAt(),
                shiftreport.getNote(),
                shiftreport.getCreatedAt()
        );
    }
}