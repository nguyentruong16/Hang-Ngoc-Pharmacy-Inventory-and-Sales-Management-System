package com.example.project.dto.response;

import com.example.project.entity.Vatinvoiceconfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VatinvoiceconfigResponse {
    private Integer id;
    private Integer branchId;
    private String series;
    private String pattern;
    private Integer currentNumber;
    private String resetCycle;

    public static VatinvoiceconfigResponse from(Vatinvoiceconfig vatinvoiceconfig) {
        return new VatinvoiceconfigResponse(
                vatinvoiceconfig.getId(),
                vatinvoiceconfig.getBranchID() != null ? vatinvoiceconfig.getBranchID().getId() : null,
                vatinvoiceconfig.getSeries(),
                vatinvoiceconfig.getPattern(),
                vatinvoiceconfig.getCurrentNumber(),
                vatinvoiceconfig.getResetCycle()
        );
    }
}