package com.example.project.service;

import com.example.project.entity.Shiftreport;
import com.example.project.repository.ShiftreportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiftreportService {
    private final ShiftreportRepository shiftreportRepository;

    public ShiftreportService(ShiftreportRepository shiftreportRepository) {
        this.shiftreportRepository = shiftreportRepository;
    }

    public List<Shiftreport> getAll() {
        return shiftreportRepository.findAll();
    }
}
