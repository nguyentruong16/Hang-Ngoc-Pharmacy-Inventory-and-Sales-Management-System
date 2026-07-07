package com.example.project.service;

import com.example.project.dto.response.StockadjustmentdetailResponse;
import com.example.project.repository.StockadjustmentdetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockadjustmentdetailService {
    private final StockadjustmentdetailRepository stockadjustmentdetailRepository;

    public StockadjustmentdetailService(StockadjustmentdetailRepository stockadjustmentdetailRepository) {
        this.stockadjustmentdetailRepository = stockadjustmentdetailRepository;
    }

    @Transactional(readOnly = true)
    public List<StockadjustmentdetailResponse> getAll() {
        return stockadjustmentdetailRepository.findAll()
                .stream()
                .map(StockadjustmentdetailResponse::from)
                .toList();
    }
}