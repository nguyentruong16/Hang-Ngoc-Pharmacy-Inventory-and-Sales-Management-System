package com.example.project.service;

import com.example.project.entity.Supplier;
import com.example.project.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> getAll() {
        return supplierRepository.findAll();
    }
}
