package com.example.project.controller;

import com.example.project.dto.response.StockoutResponse;
import com.example.project.service.StockoutService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stock-outs")
public class StockoutController {
    private final StockoutService stockoutService;

    public StockoutController(StockoutService stockoutService) {
        this.stockoutService = stockoutService;
    }

    @GetMapping
    public List<StockoutResponse> getAll() {
        return stockoutService.getAll();
    }
}