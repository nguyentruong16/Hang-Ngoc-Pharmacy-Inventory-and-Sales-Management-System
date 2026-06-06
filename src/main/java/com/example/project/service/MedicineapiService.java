package com.example.project.service;

import com.example.project.entity.Medicineapi;
import com.example.project.repository.MedicineapiRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicineapiService {
    private final MedicineapiRepository medicineapiRepository;

    public MedicineapiService(MedicineapiRepository medicineapiRepository) {
        this.medicineapiRepository = medicineapiRepository;
    }

    public List<Medicineapi> getAll() {
        return medicineapiRepository.findAll();
    }
}
