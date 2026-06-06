package com.example.project.controller;

import com.example.project.entity.Procurementplandetail;
import com.example.project.service.ProcurementplandetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/procurement-plan-details")
public class ProcurementplandetailController {
    private final ProcurementplandetailService procurementplandetailService;

    public ProcurementplandetailController(ProcurementplandetailService procurementplandetailService) {
        this.procurementplandetailService = procurementplandetailService;
    }

    @GetMapping
    public List<Procurementplandetail> getAll() {
        return procurementplandetailService.getAll();
    }
}
