package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * One already-known (lotNumber, expirationDate) pair for a product, with its current combined
 * stock across every {@link com.example.project.entity.Batch} row sharing that pair — offered on
 * the "Nhập vào lô" screen so a re-supply of the same lot can reuse its lot/expiry instead of the
 * user retyping (and risking a typo that would fragment the same physical lot into two labels).
 *
 * <p>{@code productionDate}/{@code expirationDate} are plain ISO ({@code yyyy-MM-dd}) strings, not
 * {@link java.time.LocalDate} — this response is serialized to inline JavaScript on the create
 * page via Thymeleaf's {@code th:inline="javascript"}, whose Jackson mapper has no JSR-310 module
 * registered and throws on a raw {@code LocalDate}. The string is exactly what an
 * {@code <input type="date">} expects as its value anyway.</p>
 */
@Getter
@AllArgsConstructor
public class PurchaseInvoiceExistingLotResponse {

    private String lotNumber;

    private String productionDate;
    private String productionDateDisplay;

    private String expirationDate;
    private String expirationDateDisplay;

    private long totalStock;
}
