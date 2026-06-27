package com.example.project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PurchaseInvoiceCreateRequest {

    @NotNull(message = "Vui lòng chọn nhà cung cấp")
    private Integer supplierId;

    @NotNull(message = "Vui lòng chọn chi nhánh nhập")
    private Integer branchId;

    private Integer requisitionId;

    @DecimalMin(value = "0.0", message = "Chi phí phát sinh không được âm")
    private BigDecimal additionCost = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Chiết khấu không được âm")
    private BigDecimal discount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Số tiền đã trả không được âm")
    private BigDecimal paid = BigDecimal.ZERO;

    private String note;

    @Valid
    @NotEmpty(message = "Phiếu nhập phải có ít nhất một sản phẩm")
    private List<PurchaseInvoiceDetailCreateRequest> details = new ArrayList<>();
}