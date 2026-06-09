package com.example.project.controller;

import com.example.project.dto.response.ShiftreportResponse;
import com.example.project.service.ShiftreportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shift-reports")
public class ShiftreportController {
    private final ShiftreportService shiftreportService;

    public ShiftreportController(ShiftreportService shiftreportService) {
        this.shiftreportService = shiftreportService;
    }

    @GetMapping
    public List<ShiftreportResponse> getAll() {
        return shiftreportService.getAll();
    }
}