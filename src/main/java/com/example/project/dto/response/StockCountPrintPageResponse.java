package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StockCountPrintPageResponse {

    private String printDateDisplay;

    private String printedByName;

    private long totalItems;

    private List<StockCountPrintLineResponse> items;
}