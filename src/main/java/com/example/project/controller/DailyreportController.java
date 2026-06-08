package com.example.project.controller;

import com.example.project.entity.Dailyreport;
import com.example.project.service.DailyreportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/daily-reports")
public class DailyreportController {
    private final DailyreportService dailyreportService;

    public DailyreportController(DailyreportService dailyreportService) {
        this.dailyreportService = dailyreportService;
    }

    @GetMapping
    public List<Dailyreport> getAll() {
        return dailyreportService.getAll();
    }
}