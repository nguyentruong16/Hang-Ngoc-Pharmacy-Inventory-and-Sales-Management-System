package com.example.project.service;

import com.example.project.entity.Purchaserequisition;
import com.example.project.repository.PurchaserequisitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaserequisitionService {
    private final PurchaserequisitionRepository purchaserequisitionRepository;

    public PurchaserequisitionService(PurchaserequisitionRepository purchaserequisitionRepository) {
        this.purchaserequisitionRepository = purchaserequisitionRepository;
    }

    public List<Purchaserequisition> getAll() {
        return purchaserequisitionRepository.findAll();
    }
}