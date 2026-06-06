package com.example.project.controller;

import com.example.project.entity.Unitconversion;
import com.example.project.service.UnitconversionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/unit-conversions")
public class UnitconversionController {
    private final UnitconversionService unitconversionService;

    public UnitconversionController(UnitconversionService unitconversionService) {
        this.unitconversionService = unitconversionService;
    }

    @GetMapping
    public List<Unitconversion> getAll() {
        return unitconversionService.getAll();
    }
}
