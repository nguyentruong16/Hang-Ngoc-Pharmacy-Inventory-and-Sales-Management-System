package com.example.project.service;

import com.example.project.entity.Dailyreport;
import com.example.project.repository.DailyreportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyreportService {
    private final DailyreportRepository dailyreportRepository;

    public DailyreportService(DailyreportRepository dailyreportRepository) {
        this.dailyreportRepository = dailyreportRepository;
    }

    public List<Dailyreport> getAll() {
        return dailyreportRepository.findAll();
    }
}
