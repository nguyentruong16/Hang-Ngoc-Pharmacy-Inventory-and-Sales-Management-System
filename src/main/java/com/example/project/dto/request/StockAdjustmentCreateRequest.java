package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Form backing the "create stock adjustment" screen. Carries the slip-level adjustment type,
 * reason/note plus the per-batch lines the user picked.
 */
@Getter
@Setter
public class StockAdjustmentCreateRequest {

    /** One of DESTROY / INTERNAL_USE / SAMPLE / GIFT (COUNT_* comes from stock count only). */
    private String adjustmentType;

    private String reason;

    private String note;

    private List<StockAdjustmentItemRequest> items = new ArrayList<>();
}
