package com.example.project.service;

import com.example.project.entity.Financialsetting;
import com.example.project.repository.FinancialsettingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinancialsettingService {
    private final FinancialsettingRepository financialsettingRepository;

    public FinancialsettingService(FinancialsettingRepository financialsettingRepository) {
        this.financialsettingRepository = financialsettingRepository;
    }

    public List<Financialsetting> getAll() {
        return financialsettingRepository.findAll();
    }
}