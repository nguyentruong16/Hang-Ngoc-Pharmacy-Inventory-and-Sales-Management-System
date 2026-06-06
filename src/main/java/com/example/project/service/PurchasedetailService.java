package com.example.project.service;

import com.example.project.entity.Purchasedetail;
import com.example.project.repository.PurchasedetailRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchasedetailService {
    private final PurchasedetailRepository purchasedetailRepository;

    public PurchasedetailService(PurchasedetailRepository purchasedetailRepository) {
        this.purchasedetailRepository = purchasedetailRepository;
    }

    public List<Purchasedetail> getAll() {
        return purchasedetailRepository.findAll();
    }
}
