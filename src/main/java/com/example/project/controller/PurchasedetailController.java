package com.example.project.controller;

import com.example.project.dto.response.PurchasedetailResponse;
import com.example.project.service.PurchasedetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/purchase-details")
public class PurchasedetailController {
    private final PurchasedetailService purchasedetailService;

    public PurchasedetailController(PurchasedetailService purchasedetailService) {
        this.purchasedetailService = purchasedetailService;
    }

    @GetMapping
    public List<PurchasedetailResponse> getAll() {
        return purchasedetailService.getAll();
    }
}