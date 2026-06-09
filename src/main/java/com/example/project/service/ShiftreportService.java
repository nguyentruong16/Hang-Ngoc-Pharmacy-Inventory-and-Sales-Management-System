package com.example.project.service;

import com.example.project.dto.response.ShiftreportResponse;
import com.example.project.repository.ShiftreportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShiftreportService {
    private final ShiftreportRepository shiftreportRepository;

    public ShiftreportService(ShiftreportRepository shiftreportRepository) {
        this.shiftreportRepository = shiftreportRepository;
    }

    @Transactional(readOnly = true)
    public List<ShiftreportResponse> getAll() {
        return shiftreportRepository.findAll()
                .stream()
                .map(ShiftreportResponse::from)
                .toList();
    }
}