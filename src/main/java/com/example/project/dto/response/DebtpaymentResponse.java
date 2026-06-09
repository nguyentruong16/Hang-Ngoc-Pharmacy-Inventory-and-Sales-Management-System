package com.example.project.dto.response;

import com.example.project.entity.Debtpayment;
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
public class DebtpaymentResponse {
    private Integer id;
    private Integer debtId;
    private Instant paymentDate;
    private BigDecimal amount;
    private BigDecimal paidByCash;
    private BigDecimal paidByBanking;
    private Integer incomeId;
    private Integer expenseId;
    private Integer recordedById;
    private String note;

    public static DebtpaymentResponse from(Debtpayment debtpayment) {
        return new DebtpaymentResponse(
                debtpayment.getId(),
                debtpayment.getDebtID() != null ? debtpayment.getDebtID().getId() : null,
                debtpayment.getPaymentDate(),
                debtpayment.getAmount(),
                debtpayment.getPaidByCash(),
                debtpayment.getPaidByBanking(),
                debtpayment.getIncomeID() != null ? debtpayment.getIncomeID().getId() : null,
                debtpayment.getExpenseID() != null ? debtpayment.getExpenseID().getId() : null,
                debtpayment.getRecordedBy() != null ? debtpayment.getRecordedBy().getId() : null,
                debtpayment.getNote()
        );
    }
}