package com.example.project.controller;

import com.example.project.entity.Invoicedetail;
import com.example.project.service.InvoicedetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/invoice-details")
public class InvoicedetailController {
    private final InvoicedetailService invoicedetailService;

    public InvoicedetailController(InvoicedetailService invoicedetailService) {
        this.invoicedetailService = invoicedetailService;
    }

    @GetMapping
    public List<Invoicedetail> getAll() {
        return invoicedetailService.getAll();
    }
}