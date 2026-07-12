package com.example.project.dto.response;

import com.example.project.entity.Invoice;
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
public class InvoiceResponse {
    private Integer id;
    private String invoicePattern;
    private String invoiceNumber;
    private Instant date;
    private Integer employeeId;
    private Integer customerId;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private BigDecimal paidByCash;
    private BigDecimal paidByBanking;
    private BigDecimal debtAmount;
    private Boolean prescriptionRequired;
    private String returnStatus;
    private String status;
    private Integer shiftReportId;
    private String prescriptionCode;
    private String note;

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoicePattern(),
                invoice.getInvoiceNumber(), 
                invoice.getDate(),
                invoice.getEmployeeID() != null ? invoice.getEmployeeID().getId() : null,
                invoice.getCustomerID() != null ? invoice.getCustomerID().getId() : null,
                invoice.getSubtotal(),
                invoice.getDiscount(),
                invoice.getTotal(),
                invoice.getPaidByCash(),
                invoice.getPaidByBanking(),
                invoice.getDebtAmount(),
                invoice.getPrescriptionRequired(),
                invoice.getReturnStatus(),
                invoice.getStatus(),
                invoice.getShiftReportID() != null ? invoice.getShiftReportID().getId() : null,
                invoice.getPrescriptionCode(),
                invoice.getNote()
        );
    }
}