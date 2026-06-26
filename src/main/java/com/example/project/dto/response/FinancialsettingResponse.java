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
    private BigDecimal vatRate;
    private BigDecimal incomeTaxRate ;
    private Integer taxCalculationMethod;
    private BigDecimal returnProductOnInvoiceValueRate;
    private Boolean autoGenerateVATInvoice;
    private String vatInvoiceSeries;
    private BigDecimal openingCashDefault;
    private String taxCode;
    private String locationCode;
    private String locationName;
    private String phoneNumber;
    private String email;
    private String bankAccountNumber;
    private String bankName;

    public static FinancialsettingResponse from(Financialsetting financialsetting) {
        return new FinancialsettingResponse(
                financialsetting.getId(),
                financialsetting.getVatRate(),
                financialsetting.getIncomeTaxRate(),
                financialsetting.getTaxCalculationMethod(),
                financialsetting.getReturnProductOnInvoiceValueRate(),
                financialsetting.getAutoGenerateVATInvoice(),
                financialsetting.getVatInvoiceSeries(),
                financialsetting.getOpeningCashDefault(),
                financialsetting.getTaxCode(),
                financialsetting.getLocationCode(),
                financialsetting.getLocationName(),
                financialsetting.getPhoneNumber(),
                financialsetting.getEmail(),
                financialsetting.getBankAccountNumber(),
                financialsetting.getBankName()
        );
    }
}