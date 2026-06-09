package com.example.project.service;

import com.example.project.dto.response.DebtResponse;
import com.example.project.repository.DebtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DebtService {
    private final DebtRepository debtRepository;

    public DebtService(DebtRepository debtRepository) {
        this.debtRepository = debtRepository;
    }

    @Transactional(readOnly = true)
    public List<DebtResponse> getAll() {
        return debtRepository.findAll()
                .stream()
                .map(DebtResponse::from)
                .toList();
    }
}