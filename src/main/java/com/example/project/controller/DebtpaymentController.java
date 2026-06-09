package com.example.project.controller;

import com.example.project.dto.response.DebtpaymentResponse;
import com.example.project.service.DebtpaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/debt-payments")
public class DebtpaymentController {
    private final DebtpaymentService debtpaymentService;

    public DebtpaymentController(DebtpaymentService debtpaymentService) {
        this.debtpaymentService = debtpaymentService;
    }

    @GetMapping
    public List<DebtpaymentResponse> getAll() {
        return debtpaymentService.getAll();
    }
}