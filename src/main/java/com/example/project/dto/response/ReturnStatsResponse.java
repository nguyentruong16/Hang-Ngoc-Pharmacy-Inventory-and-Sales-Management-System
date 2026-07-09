package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Header counters on the customer-return list. */
@Getter
@AllArgsConstructor
public class ReturnStatsResponse {

    private long monthlyCount;
    private long draftCount;
    private long pendingCount;
    private long debtCount;
    private long rejectedCount;
}
