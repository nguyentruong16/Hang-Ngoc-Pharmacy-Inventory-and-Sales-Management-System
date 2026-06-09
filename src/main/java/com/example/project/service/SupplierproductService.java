package com.example.project.service;

import com.example.project.dto.response.SupplierproductResponse;
import com.example.project.repository.SupplierproductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierproductService {
    private final SupplierproductRepository supplierproductRepository;

    public SupplierproductService(SupplierproductRepository supplierproductRepository) {
        this.supplierproductRepository = supplierproductRepository;
    }

    @Transactional(readOnly = true)
    public List<SupplierproductResponse> getAll() {
        return supplierproductRepository.findAll()
                .stream()
                .map(SupplierproductResponse::from)
                .toList();
    }
}