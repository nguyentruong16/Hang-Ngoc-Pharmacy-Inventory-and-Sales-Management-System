package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * One already-known (lotNumber, expirationDate) pair for a product, with its current combined
 * stock across every {@link com.example.project.entity.Batch} row sharing that pair — offered on
 * the "Nhập vào lô" screen so a re-supply of the same lot can reuse its lot/expiry instead of the
 * user retyping (and risking a typo that would fragment the same physical lot into two labels).
 */
@Getter
@AllArgsConstructor
public class PurchaseInvoiceExistingLotResponse {

    private String lotNumber;

    private LocalDate productionDate;
    private String productionDateDisplay;

    private LocalDate expirationDate;
    private String expirationDateDisplay;

    private long totalStock;
}
