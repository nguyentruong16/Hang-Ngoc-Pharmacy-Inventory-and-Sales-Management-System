package com.example.project.controller;

import com.example.project.entity.Purchaseinvoice;
import com.example.project.service.PurchaseinvoiceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/purchase-invoices")
public class PurchaseinvoiceController {
    private final PurchaseinvoiceService purchaseinvoiceService;

    public PurchaseinvoiceController(PurchaseinvoiceService purchaseinvoiceService) {
        this.purchaseinvoiceService = purchaseinvoiceService;
    }

    @GetMapping
    public List<Purchaseinvoice> getAll() {
        return purchaseinvoiceService.getAll();
    }
}
