package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class StockOutDetailItemResponse {

    private Integer productId;
    private String productName;
    private String lotNumber;
    private LocalDate expirationDate;
    private String expirationDateDisplay;
    private String unitName;
    private Integer quantity;
    private BigDecimal unitCostPrice;
    private BigDecimal lineCost;
    private String note;
}