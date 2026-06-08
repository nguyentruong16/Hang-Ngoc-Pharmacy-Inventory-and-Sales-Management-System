package com.example.project.service;

import com.example.project.entity.Supplierproduct;
import com.example.project.repository.SupplierproductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierproductService {
    private final SupplierproductRepository supplierproductRepository;

    public SupplierproductService(SupplierproductRepository supplierproductRepository) {
        this.supplierproductRepository = supplierproductRepository;
    }

    public List<Supplierproduct> getAll() {
        return supplierproductRepository.findAll();
    }
}