package com.example.project.service;

import com.example.project.entity.Combocomponent;
import com.example.project.repository.CombocomponentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CombocomponentService {
    private final CombocomponentRepository combocomponentRepository;

    public CombocomponentService(CombocomponentRepository combocomponentRepository) {
        this.combocomponentRepository = combocomponentRepository;
    }

    public List<Combocomponent> getAll() {
        return combocomponentRepository.findAll();
    }
}
