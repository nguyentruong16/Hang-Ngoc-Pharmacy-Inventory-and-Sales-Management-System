package com.example.project.service;

import com.example.project.entity.Vatinvoice;
import com.example.project.repository.VatinvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VatinvoiceService {
    private final VatinvoiceRepository vatinvoiceRepository;

    public VatinvoiceService(VatinvoiceRepository vatinvoiceRepository) {
        this.vatinvoiceRepository = vatinvoiceRepository;
    }

    public List<Vatinvoice> getAll() {
        return vatinvoiceRepository.findAll();
    }
}