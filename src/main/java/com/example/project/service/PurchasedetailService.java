package com.example.project.service;

import com.example.project.dto.response.PurchasedetailResponse;
import com.example.project.repository.PurchasedetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchasedetailService {
    private final PurchasedetailRepository purchasedetailRepository;

    public PurchasedetailService(PurchasedetailRepository purchasedetailRepository) {
        this.purchasedetailRepository = purchasedetailRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchasedetailResponse> getAll() {
        return purchasedetailRepository.findAll()
                .stream()
                .map(PurchasedetailResponse::from)
                .toList();
    }
}