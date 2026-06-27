package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockOutDestroyItemRequest {

    private Integer batchId;

    private Integer quantity;

    private String reason;
}