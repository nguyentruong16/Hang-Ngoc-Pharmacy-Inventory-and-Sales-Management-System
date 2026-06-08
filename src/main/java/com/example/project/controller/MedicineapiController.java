package com.example.project.controller;

import com.example.project.entity.Medicineapi;
import com.example.project.service.MedicineapiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/medicine-apis")
public class MedicineapiController {
    private final MedicineapiService medicineapiService;

    public MedicineapiController(MedicineapiService medicineapiService) {
        this.medicineapiService = medicineapiService;
    }

    @GetMapping
    public List<Medicineapi> getAll() {
        return medicineapiService.getAll();
    }
}