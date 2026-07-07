package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Form backing the "create stock adjustment" screen. Carries the slip-level reason/note plus the
 * per-batch lines the user picked. The adjustment type (currently DESTROY) is decided server-side.
 */
@Getter
@Setter
public class StockAdjustmentCreateRequest {

    private String reason;

    private String note;

    private List<StockAdjustmentItemRequest> items = new ArrayList<>();
}
