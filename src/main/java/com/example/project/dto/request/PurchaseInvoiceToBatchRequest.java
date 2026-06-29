package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PurchaseInvoiceToBatchRequest {

    private List<PurchaseInvoiceToBatchItemRequest> items = new ArrayList<>();
}