package com.example.project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** The sell (create-invoice) form. A null {@code customerId} means "khách lẻ" (walk-in). */
@Getter
@Setter
public class InvoiceCreateRequest {

    private Integer customerId;

    @DecimalMin(value = "0.0", message = "Chiết khấu không được âm")
    private BigDecimal discount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Tiền mặt không được âm")
    private BigDecimal paidByCash = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Chuyển khoản không được âm")
    private BigDecimal paidByBanking = BigDecimal.ZERO;

    private Boolean prescriptionRequired = Boolean.FALSE;

    private String prescriptionCode;

    private String note;

    @Valid
    @NotEmpty(message = "Hóa đơn phải có ít nhất một sản phẩm")
    private List<InvoiceDetailCreateRequest> details = new ArrayList<>();
}
