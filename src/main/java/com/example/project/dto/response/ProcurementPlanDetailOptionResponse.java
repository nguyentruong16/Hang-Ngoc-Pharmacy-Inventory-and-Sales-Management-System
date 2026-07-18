package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * One {@code Procurementplandetail} line offered back to the Purchase Invoice create form once
 * both a supplier and a procurement plan are selected — the plan's requested quantity/price for
 * that supplier's products, used only to suggest/prefill a detail row (see
 * {@code PurchaseinvoiceService#getProcurementPlanDetailsForSupplier}). Never authoritative: the
 * user can still edit every prefilled value before saving.
 */
@Getter
@AllArgsConstructor
public class ProcurementPlanDetailOptionResponse {

    private Integer productId;
    private String productName;
    private Integer requestedQuantity;
    private String unit;

    /** Total estimated price for {@code requestedQuantity}, VAT-inclusive (as recorded on the plan). */
    private BigDecimal estimatedPrice;

    /** {@code estimatedPrice ÷ requestedQuantity} — VAT-inclusive, directly usable as "Giá nhập". */
    private BigDecimal unitPrice;
}
