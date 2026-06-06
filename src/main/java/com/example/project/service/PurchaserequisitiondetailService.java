package com.example.project.service;

import com.example.project.entity.Purchaserequisitiondetail;
import com.example.project.repository.PurchaserequisitiondetailRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaserequisitiondetailService {
    private final PurchaserequisitiondetailRepository purchaserequisitiondetailRepository;

    public PurchaserequisitiondetailService(PurchaserequisitiondetailRepository purchaserequisitiondetailRepository) {
        this.purchaserequisitiondetailRepository = purchaserequisitiondetailRepository;
    }

    public List<Purchaserequisitiondetail> getAll() {
        return purchaserequisitiondetailRepository.findAll();
    }
}
