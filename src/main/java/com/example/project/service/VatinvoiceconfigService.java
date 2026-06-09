package com.example.project.service;

import com.example.project.dto.response.VatinvoiceconfigResponse;
import com.example.project.repository.VatinvoiceconfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VatinvoiceconfigService {
    private final VatinvoiceconfigRepository vatinvoiceconfigRepository;

    public VatinvoiceconfigService(VatinvoiceconfigRepository vatinvoiceconfigRepository) {
        this.vatinvoiceconfigRepository = vatinvoiceconfigRepository;
    }

    @Transactional(readOnly = true)
    public List<VatinvoiceconfigResponse> getAll() {
        return vatinvoiceconfigRepository.findAll()
                .stream()
                .map(VatinvoiceconfigResponse::from)
                .toList();
    }
}