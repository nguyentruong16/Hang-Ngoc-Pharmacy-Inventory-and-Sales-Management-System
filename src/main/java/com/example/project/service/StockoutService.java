package com.example.project.service;

import com.example.project.entity.Stockout;
import com.example.project.repository.StockoutRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockoutService {
    private final StockoutRepository stockoutRepository;

    public StockoutService(StockoutRepository stockoutRepository) {
        this.stockoutRepository = stockoutRepository;
    }

    public List<Stockout> getAll() {
        return stockoutRepository.findAll();
    }
}