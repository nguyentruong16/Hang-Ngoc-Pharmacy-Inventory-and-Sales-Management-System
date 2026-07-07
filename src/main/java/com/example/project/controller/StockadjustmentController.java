package com.example.project.controller;

import com.example.project.dto.response.StockadjustmentResponse;
import com.example.project.service.StockadjustmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stock-outs")
public class StockadjustmentController {
    private final StockadjustmentService stockadjustmentService;

    public StockadjustmentController(StockadjustmentService stockadjustmentService) {
        this.stockadjustmentService = stockadjustmentService;
    }

    @GetMapping
    public List<StockadjustmentResponse> getAll() {
        return stockadjustmentService.getAll();
    }
}