package com.example.project.service;

import com.example.project.dto.response.CombocomponentResponse;
import com.example.project.repository.CombocomponentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CombocomponentService {
    private final CombocomponentRepository combocomponentRepository;

    public CombocomponentService(CombocomponentRepository combocomponentRepository) {
        this.combocomponentRepository = combocomponentRepository;
    }

    @Transactional(readOnly = true)
    public List<CombocomponentResponse> getAll() {
        return combocomponentRepository.findAll()
                .stream()
                .map(CombocomponentResponse::from)
                .toList();
    }
}