package com.example.project.service;

import com.example.project.entity.Productunit;
import com.example.project.repository.ProductunitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductunitService {
    private final ProductunitRepository productunitRepository;

    public ProductunitService(ProductunitRepository productunitRepository) {
        this.productunitRepository = productunitRepository;
    }

    public List<Productunit> getAll() {
        return productunitRepository.findAll();
    }
}