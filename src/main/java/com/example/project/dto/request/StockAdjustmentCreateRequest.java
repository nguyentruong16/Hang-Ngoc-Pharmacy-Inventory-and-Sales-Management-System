package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Form backing the "create stock adjustment" screen. Carries the slip-level adjustment type,
 * reason/note plus the per-batch lines the user picked (manual source) — or a chosen stock count
 * (stock-count source), from which the server rebuilds the lines itself.
 */
@Getter
@Setter
public class StockAdjustmentCreateRequest {

    /** Source of the lines: {@code MANUAL} (default, user picks batches) or {@code STOCK_COUNT}. */
    private String sourceMode;

    /** When {@code sourceMode == STOCK_COUNT}: the approved stock count to adjust from. */
    private Integer stockCountId;

    /** One of DESTROY / INTERNAL_USE / SAMPLE / GIFT (COUNT_* is derived from the stock count). */
    private String adjustmentType;

    private String reason;

    private String note;

    private List<StockAdjustmentItemRequest> items = new ArrayList<>();
}
