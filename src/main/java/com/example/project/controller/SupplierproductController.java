package com.example.project.controller;

import com.example.project.dto.response.SupplierproductResponse;
import com.example.project.service.SupplierproductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/supplier-products")
public class SupplierproductController {
    private final SupplierproductService supplierproductService;

    public SupplierproductController(SupplierproductService supplierproductService) {
        this.supplierproductService = supplierproductService;
    }

    @GetMapping
    public List<SupplierproductResponse> getAll() {
        return supplierproductService.getAll();
    }
}