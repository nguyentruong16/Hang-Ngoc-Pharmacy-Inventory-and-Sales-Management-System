package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class StockOutDestroyCandidateResponse {

    private Integer batchId;

    private Integer productId;
    private String productName;

    private String lotNumber;

    private LocalDate expirationDate;
    private String expirationDateDisplay;

    private Integer storageQuantity;

    private Integer productUnitId;
    private String unitName;

    private BigDecimal unitCostPrice;
}