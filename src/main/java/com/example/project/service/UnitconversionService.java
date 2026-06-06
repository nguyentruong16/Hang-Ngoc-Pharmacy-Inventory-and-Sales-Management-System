package com.example.project.service;

import com.example.project.entity.Unitconversion;
import com.example.project.repository.UnitconversionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnitconversionService {
    private final UnitconversionRepository unitconversionRepository;

    public UnitconversionService(UnitconversionRepository unitconversionRepository) {
        this.unitconversionRepository = unitconversionRepository;
    }

    public List<Unitconversion> getAll() {
        return unitconversionRepository.findAll();
    }
}
