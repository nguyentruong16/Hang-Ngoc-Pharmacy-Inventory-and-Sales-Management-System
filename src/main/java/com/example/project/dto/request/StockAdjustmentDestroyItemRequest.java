package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockAdjustmentDestroyItemRequest {

    private Integer batchId;

    private Integer quantity;

    private String reason;
}