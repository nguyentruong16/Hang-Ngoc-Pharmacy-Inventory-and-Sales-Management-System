package com.example.project.dto.response;

import com.example.project.entity.Debt;
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
public class DebtResponse {
    private Integer id;
    private String debtType;
    private Integer customerId;
    private Integer supplierId;
    private Integer invoiceId;
    private Integer purchaseInvoiceId;
    private Integer returnId;
    private Integer branchId;
    private BigDecimal originalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDate dueDate;
    private Integer statusId;
    private Integer createdById;
    private Instant createdAt;
    private String note;

    public static DebtResponse from(Debt debt) {
        return new DebtResponse(
                debt.getId(),
                debt.getDebtType(),
                debt.getCustomerID() != null ? debt.getCustomerID().getId() : null,
                debt.getSupplierID() != null ? debt.getSupplierID().getId() : null,
                debt.getInvoiceID() != null ? debt.getInvoiceID().getId() : null,
                debt.getPurchaseInvoiceID() != null ? debt.getPurchaseInvoiceID().getId() : null,
                debt.getReturnID() != null ? debt.getReturnID().getId() : null,
                debt.getBranchID() != null ? debt.getBranchID().getId() : null,
                debt.getOriginalAmount(),
                debt.getPaidAmount(),
                debt.getRemainingAmount(),
                debt.getDueDate(),
                debt.getStatusID() != null ? debt.getStatusID().getId() : null,
                debt.getCreatedBy() != null ? debt.getCreatedBy().getId() : null,
                debt.getCreatedAt(),
                debt.getNote()
        );
    }
}