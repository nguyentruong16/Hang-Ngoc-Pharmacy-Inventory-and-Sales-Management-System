package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * One editable row on the Price Settings screen ({@code /owner/price-settings}) — one
 * {@code ProductUnit}. {@code latestImportPricePerBase} is read-only reference info (the most
 * recent {@code Batch.importPricePerBase} for this product, which is GROSS/VAT-inclusive per the
 * team's confirmed convention — see CLAUDE.md), shown so the Owner has context when deciding a new
 * {@code sellPrice}; it is never written back.
 */
@Getter
@AllArgsConstructor
public class PriceSettingRowResponse {

    private Integer productUnitId;
    private Integer productId;
    private String productCode;
    private String productName;
    private String typeName;

    private String unitName;
    private boolean baseUnit;

    private BigDecimal currentSellPrice;

    private BigDecimal latestImportPricePerBase;
    private String latestImportDateDisplay;
}
