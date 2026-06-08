package com.example.project.controller;

import com.example.project.entity.Procurementplan;
import com.example.project.service.ProcurementplanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/procurement-plans")
public class ProcurementplanController {
    private final ProcurementplanService procurementplanService;

    public ProcurementplanController(ProcurementplanService procurementplanService) {
        this.procurementplanService = procurementplanService;
    }

    @GetMapping
    public List<Procurementplan> getAll() {
        return procurementplanService.getAll();
    }
}