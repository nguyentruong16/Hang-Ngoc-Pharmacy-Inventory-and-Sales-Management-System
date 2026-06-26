package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockOutStatsResponse {

    private long monthlyCount;
    private long pendingCount;
    private long reconciledCount;
    private long needCheckCount;
}