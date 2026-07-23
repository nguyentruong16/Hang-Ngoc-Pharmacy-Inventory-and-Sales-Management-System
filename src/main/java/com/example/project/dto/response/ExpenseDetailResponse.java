package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ExpenseDetailResponse {

    private Integer id;
    private String code;
    private String dateDisplay;

    private String expenseType;
    private String expenseTypeDisplay;

    private String applicantName;

    private String reason;

    private BigDecimal amount;
    private BigDecimal paid;
    private BigDecimal paidByCash;
    private BigDecimal paidByBanking;

    private String statusName;
    private String statusCssClass;

    private String approvedByName;
    private String approvedAtDisplay;

    private String note;
}
