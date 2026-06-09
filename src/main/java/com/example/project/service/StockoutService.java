package com.example.project.service;

import com.example.project.dto.response.StockoutResponse;
import com.example.project.repository.StockoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockoutService {
    private final StockoutRepository stockoutRepository;

    public StockoutService(StockoutRepository stockoutRepository) {
        this.stockoutRepository = stockoutRepository;
    }

    @Transactional(readOnly = true)
    public List<StockoutResponse> getAll() {
        return stockoutRepository.findAll()
                .stream()
                .map(StockoutResponse::from)
                .toList();
    }
}