package com.example.project.service;

import com.example.project.dto.response.IncomeResponse;
import com.example.project.repository.IncomeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IncomeService {
    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    @Transactional(readOnly = true)
    public List<IncomeResponse> getAll() {
        return incomeRepository.findAll()
                .stream()
                .map(IncomeResponse::from)
                .toList();
    }
}