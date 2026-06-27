package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PurchaseInvoiceToBatchItemRequest {

    private Integer purchaseDetailId;

    private String lotNumber;

    private LocalDate productionDate;

    private LocalDate expirationDate;
}