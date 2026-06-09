package com.example.project.service;

import com.example.project.dto.response.ProductunitResponse;
import com.example.project.repository.ProductunitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductunitService {
    private final ProductunitRepository productunitRepository;

    public ProductunitService(ProductunitRepository productunitRepository) {
        this.productunitRepository = productunitRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductunitResponse> getAll() {
        return productunitRepository.findAll()
                .stream()
                .map(ProductunitResponse::from)
                .toList();
    }
}