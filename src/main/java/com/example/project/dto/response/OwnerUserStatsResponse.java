package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OwnerUserStatsResponse {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;

    private long pharmacistUsers;
    private long accountantUsers;
}