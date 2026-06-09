package com.example.project.service;

import com.example.project.dto.response.ComboResponse;
import com.example.project.repository.ComboRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComboService {
    private final ComboRepository comboRepository;

    public ComboService(ComboRepository comboRepository) {
        this.comboRepository = comboRepository;
    }

    @Transactional(readOnly = true)
    public List<ComboResponse> getAll() {
        return comboRepository.findAll()
                .stream()
                .map(ComboResponse::from)
                .toList();
    }
}