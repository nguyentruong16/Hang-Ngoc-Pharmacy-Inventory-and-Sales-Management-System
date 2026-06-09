package com.example.project.service;

import com.example.project.dto.response.PurchaserequisitionResponse;
import com.example.project.repository.PurchaserequisitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchaserequisitionService {
    private final PurchaserequisitionRepository purchaserequisitionRepository;

    public PurchaserequisitionService(PurchaserequisitionRepository purchaserequisitionRepository) {
        this.purchaserequisitionRepository = purchaserequisitionRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaserequisitionResponse> getAll() {
        return purchaserequisitionRepository.findAll()
                .stream()
                .map(PurchaserequisitionResponse::from)
                .toList();
    }
}