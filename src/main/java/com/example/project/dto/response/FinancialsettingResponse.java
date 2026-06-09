package com.example.project.dto.response;

import com.example.project.entity.Financialsetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinancialsettingResponse {
    private Integer id;
    private Integer branchId;
    private BigDecimal vatRate;
    private BigDecimal prescriptionVatRate;
    private Integer taxCalculationMethod;
    private BigDecimal returnProductOnInvoiceValueRate;
    private Boolean autoGenerateVATInvoice;
    private String vatInvoiceSeries;
    private Integer vatInvoiceLastNumber;
    private BigDecimal openingCashDefault;

    public static FinancialsettingResponse from(Financialsetting financialsetting) {
        return new FinancialsettingResponse(
                financialsetting.getId(),
                financialsetting.getBranchID() != null ? financialsetting.getBranchID().getId() : null,
                financialsetting.getVatRate(),
                financialsetting.getPrescriptionVatRate(),
                financialsetting.getTaxCalculationMethod(),
                financialsetting.getReturnProductOnInvoiceValueRate(),
                financialsetting.getAutoGenerateVATInvoice(),
                financialsetting.getVatInvoiceSeries(),
                financialsetting.getVatInvoiceLastNumber(),
                financialsetting.getOpeningCashDefault()
        );
    }
}