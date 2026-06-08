package com.example.project.service;

import com.example.project.entity.Vatinvoiceconfig;
import com.example.project.repository.VatinvoiceconfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VatinvoiceconfigService {
    private final VatinvoiceconfigRepository vatinvoiceconfigRepository;

    public VatinvoiceconfigService(VatinvoiceconfigRepository vatinvoiceconfigRepository) {
        this.vatinvoiceconfigRepository = vatinvoiceconfigRepository;
    }

    public List<Vatinvoiceconfig> getAll() {
        return vatinvoiceconfigRepository.findAll();
    }
}