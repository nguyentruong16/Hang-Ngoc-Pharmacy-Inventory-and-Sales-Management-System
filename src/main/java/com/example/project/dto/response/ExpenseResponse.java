package com.example.project.dto.response;

import com.example.project.entity.Expense;
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
public class ExpenseResponse {
    private Integer id;
    private Integer branchId;
    private Integer applicantId;
    private String expenseType;
    private Integer returnId;
    private Integer shiftReportId;
    private Instant date;
    private String reason;
    private BigDecimal amount;
    private BigDecimal paid;
    private BigDecimal paidByCash;
    private BigDecimal paidByBanking;
    private String expenseCategoryCode;
    private Integer statusId;
    private Integer approvedById;
    private Instant approvedAt;
    private String note;

    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getBranchID() != null ? expense.getBranchID().getId() : null,
                expense.getApplicantID() != null ? expense.getApplicantID().getId() : null,
                expense.getExpenseType(),
                expense.getReturnID() != null ? expense.getReturnID().getId() : null,
                expense.getShiftReportID() != null ? expense.getShiftReportID().getId() : null,
                expense.getDate(),
                expense.getReason(),
                expense.getAmount(),
                expense.getPaid(),
                expense.getPaidByCash(),
                expense.getPaidByBanking(),
                expense.getExpenseCategoryCode(),
                expense.getStatusID() != null ? expense.getStatusID().getId() : null,
                expense.getApprovedBy() != null ? expense.getApprovedBy().getId() : null,
                expense.getApprovedAt(),
                expense.getNote()
        );
    }
}