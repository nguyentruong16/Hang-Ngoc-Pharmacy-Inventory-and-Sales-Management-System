package com.example.project.service;

import com.example.project.dto.response.FinancialsettingResponse;
import com.example.project.repository.FinancialsettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FinancialsettingService {
    private final FinancialsettingRepository financialsettingRepository;

    public FinancialsettingService(FinancialsettingRepository financialsettingRepository) {
        this.financialsettingRepository = financialsettingRepository;
    }

    @Transactional(readOnly = true)
    public List<FinancialsettingResponse> getAll() {
        return financialsettingRepository.findAll()
                .stream()
                .map(FinancialsettingResponse::from)
                .toList();
    }
}