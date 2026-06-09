package com.example.project.controller;

import com.example.project.dto.response.ProductunitResponse;
import com.example.project.service.ProductunitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product-units")
public class ProductunitController {
    private final ProductunitService productunitService;

    public ProductunitController(ProductunitService productunitService) {
        this.productunitService = productunitService;
    }

    @GetMapping
    public List<ProductunitResponse> getAll() {
        return productunitService.getAll();
    }
}