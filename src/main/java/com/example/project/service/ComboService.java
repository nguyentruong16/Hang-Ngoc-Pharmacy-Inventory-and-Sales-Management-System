package com.example.project.service;

import com.example.project.entity.Combo;
import com.example.project.repository.ComboRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComboService {
    private final ComboRepository comboRepository;

    public ComboService(ComboRepository comboRepository) {
        this.comboRepository = comboRepository;
    }

    public List<Combo> getAll() {
        return comboRepository.findAll();
    }
}