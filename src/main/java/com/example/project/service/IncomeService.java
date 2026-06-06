package com.example.project.service;

import com.example.project.entity.Income;
import com.example.project.repository.IncomeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncomeService {
    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public List<Income> getAll() {
        return incomeRepository.findAll();
    }
}
