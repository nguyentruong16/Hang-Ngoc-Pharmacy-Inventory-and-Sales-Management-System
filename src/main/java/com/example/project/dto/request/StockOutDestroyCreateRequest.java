package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StockOutDestroyCreateRequest {

    private String reason;

    private String note;

    private List<StockOutDestroyItemRequest> items = new ArrayList<>();
}