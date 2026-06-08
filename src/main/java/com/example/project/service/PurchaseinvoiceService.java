package com.example.project.service;

import com.example.project.entity.Purchaseinvoice;
import com.example.project.repository.PurchaseinvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseinvoiceService {
    private final PurchaseinvoiceRepository purchaseinvoiceRepository;

    public PurchaseinvoiceService(PurchaseinvoiceRepository purchaseinvoiceRepository) {
        this.purchaseinvoiceRepository = purchaseinvoiceRepository;
    }

    public List<Purchaseinvoice> getAll() {
        return purchaseinvoiceRepository.findAll();
    }
}