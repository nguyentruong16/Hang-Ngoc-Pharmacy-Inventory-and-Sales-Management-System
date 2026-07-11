package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** KPI counters for the supplier-return list screen. */
@Getter
@AllArgsConstructor
public class ReturnPurchaseStatsResponse {

    private long monthlyCount;
    private long draftCount;
    private long approvedCount;
    private long rejectedCount;
}
