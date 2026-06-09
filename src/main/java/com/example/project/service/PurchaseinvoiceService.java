package com.example.project.service;

import com.example.project.dto.response.PurchaseinvoiceResponse;
import com.example.project.repository.PurchaseinvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchaseinvoiceService {
    private final PurchaseinvoiceRepository purchaseinvoiceRepository;

    public PurchaseinvoiceService(PurchaseinvoiceRepository purchaseinvoiceRepository) {
        this.purchaseinvoiceRepository = purchaseinvoiceRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaseinvoiceResponse> getAll() {
        return purchaseinvoiceRepository.findAll()
                .stream()
                .map(PurchaseinvoiceResponse::from)
                .toList();
    }
}