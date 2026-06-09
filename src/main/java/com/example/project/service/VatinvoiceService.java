package com.example.project.service;

import com.example.project.dto.response.VatinvoiceResponse;
import com.example.project.repository.VatinvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VatinvoiceService {
    private final VatinvoiceRepository vatinvoiceRepository;

    public VatinvoiceService(VatinvoiceRepository vatinvoiceRepository) {
        this.vatinvoiceRepository = vatinvoiceRepository;
    }

    @Transactional(readOnly = true)
    public List<VatinvoiceResponse> getAll() {
        return vatinvoiceRepository.findAll()
                .stream()
                .map(VatinvoiceResponse::from)
                .toList();
    }
}