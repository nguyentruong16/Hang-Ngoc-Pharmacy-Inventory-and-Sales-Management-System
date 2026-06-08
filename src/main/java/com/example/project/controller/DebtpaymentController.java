package com.example.project.controller;

import com.example.project.entity.Debtpayment;
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
    public List<Debtpayment> getAll() {
        return debtpaymentService.getAll();
    }
}