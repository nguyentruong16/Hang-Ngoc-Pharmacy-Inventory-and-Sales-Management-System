package com.example.project.service;

import com.example.project.dto.response.StockoutdetailResponse;
import com.example.project.repository.StockoutdetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockoutdetailService {
    private final StockoutdetailRepository stockoutdetailRepository;

    public StockoutdetailService(StockoutdetailRepository stockoutdetailRepository) {
        this.stockoutdetailRepository = stockoutdetailRepository;
    }

    @Transactional(readOnly = true)
    public List<StockoutdetailResponse> getAll() {
        return stockoutdetailRepository.findAll()
                .stream()
                .map(StockoutdetailResponse::from)
                .toList();
    }
}