package com.example.project.controller;

import com.example.project.dto.response.VatinvoiceconfigResponse;
import com.example.project.service.VatinvoiceconfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vat-invoice-configs")
public class VatinvoiceconfigController {
    private final VatinvoiceconfigService vatinvoiceconfigService;

    public VatinvoiceconfigController(VatinvoiceconfigService vatinvoiceconfigService) {
        this.vatinvoiceconfigService = vatinvoiceconfigService;
    }

    @GetMapping
    public List<VatinvoiceconfigResponse> getAll() {
        return vatinvoiceconfigService.getAll();
    }
}