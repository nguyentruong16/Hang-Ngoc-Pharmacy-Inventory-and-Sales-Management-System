package com.example.project.controller;

import com.example.project.entity.Financialsetting;
import com.example.project.service.FinancialsettingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/financial-settings")
public class FinancialsettingController {
    private final FinancialsettingService financialsettingService;

    public FinancialsettingController(FinancialsettingService financialsettingService) {
        this.financialsettingService = financialsettingService;
    }

    @GetMapping
    public List<Financialsetting> getAll() {
        return financialsettingService.getAll();
    }
}