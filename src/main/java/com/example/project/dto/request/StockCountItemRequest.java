package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockCountItemRequest {

    private Integer batchId;

    private Integer actualQty;

    private String note;
}