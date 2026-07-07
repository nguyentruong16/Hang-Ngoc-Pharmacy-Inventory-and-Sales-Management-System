package com.example.project.controller;

import com.example.project.dto.response.StockadjustmentdetailResponse;
import com.example.project.service.StockadjustmentdetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stock-out-details")
public class StockadjustmentdetailController {
    private final StockadjustmentdetailService stockadjustmentdetailService;

    public StockadjustmentdetailController(StockadjustmentdetailService stockadjustmentdetailService) {
        this.stockadjustmentdetailService = stockadjustmentdetailService;
    }

    @GetMapping
    public List<StockadjustmentdetailResponse> getAll() {
        return stockadjustmentdetailService.getAll();
    }
}