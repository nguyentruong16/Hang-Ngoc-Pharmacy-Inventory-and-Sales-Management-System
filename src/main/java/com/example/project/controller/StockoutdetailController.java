package com.example.project.controller;

import com.example.project.entity.Stockoutdetail;
import com.example.project.service.StockoutdetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stock-out-details")
public class StockoutdetailController {
    private final StockoutdetailService stockoutdetailService;

    public StockoutdetailController(StockoutdetailService stockoutdetailService) {
        this.stockoutdetailService = stockoutdetailService;
    }

    @GetMapping
    public List<Stockoutdetail> getAll() {
        return stockoutdetailService.getAll();
    }
}