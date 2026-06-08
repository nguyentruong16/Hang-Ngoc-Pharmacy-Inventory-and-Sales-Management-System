package com.example.project.service;

import com.example.project.entity.Debt;
import com.example.project.repository.DebtRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtService {
    private final DebtRepository debtRepository;

    public DebtService(DebtRepository debtRepository) {
        this.debtRepository = debtRepository;
    }

    public List<Debt> getAll() {
        return debtRepository.findAll();
    }
}