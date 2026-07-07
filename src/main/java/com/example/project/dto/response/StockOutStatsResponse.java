package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockOutStatsResponse {

    private long monthlyCount;
    private long draftCount;
    private long approvedCount;
    private long rejectedCount;
}