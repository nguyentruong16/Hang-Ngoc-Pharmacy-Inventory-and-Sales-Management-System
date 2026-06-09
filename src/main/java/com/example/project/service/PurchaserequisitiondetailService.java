package com.example.project.service;

import com.example.project.dto.response.PurchaserequisitiondetailResponse;
import com.example.project.repository.PurchaserequisitiondetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchaserequisitiondetailService {
    private final PurchaserequisitiondetailRepository purchaserequisitiondetailRepository;

    public PurchaserequisitiondetailService(PurchaserequisitiondetailRepository purchaserequisitiondetailRepository) {
        this.purchaserequisitiondetailRepository = purchaserequisitiondetailRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaserequisitiondetailResponse> getAll() {
        return purchaserequisitiondetailRepository.findAll()
                .stream()
                .map(PurchaserequisitiondetailResponse::from)
                .toList();
    }
}