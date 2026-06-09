package com.example.project.controller;

import com.example.project.dto.response.VatinvoiceResponse;
import com.example.project.service.VatinvoiceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vat-invoices")
public class VatinvoiceController {
    private final VatinvoiceService vatinvoiceService;

    public VatinvoiceController(VatinvoiceService vatinvoiceService) {
        this.vatinvoiceService = vatinvoiceService;
    }

    @GetMapping
    public List<VatinvoiceResponse> getAll() {
        return vatinvoiceService.getAll();
    }
}