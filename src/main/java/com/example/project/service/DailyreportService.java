package com.example.project.service;

import com.example.project.dto.response.DailyreportResponse;
import com.example.project.repository.DailyreportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DailyreportService {
    private final DailyreportRepository dailyreportRepository;

    public DailyreportService(DailyreportRepository dailyreportRepository) {
        this.dailyreportRepository = dailyreportRepository;
    }

    @Transactional(readOnly = true)
    public List<DailyreportResponse> getAll() {
        return dailyreportRepository.findAll()
                .stream()
                .map(DailyreportResponse::from)
                .toList();
    }
}