package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApprovalStatsResponse {

    private long totalCount;

    private long returnCount;

    private long stockAdjustmentCount;

    private long stockCountCount;

    private long shiftReportCount;

    private long expenseCount;
}
