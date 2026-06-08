package com.example.project.service;

import com.example.project.entity.Stockoutdetail;
import com.example.project.repository.StockoutdetailRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockoutdetailService {
    private final StockoutdetailRepository stockoutdetailRepository;

    public StockoutdetailService(StockoutdetailRepository stockoutdetailRepository) {
        this.stockoutdetailRepository = stockoutdetailRepository;
    }

    public List<Stockoutdetail> getAll() {
        return stockoutdetailRepository.findAll();
    }
}