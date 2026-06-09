package com.example.project.service;

import com.example.project.dto.response.MedicineapiResponse;
import com.example.project.repository.MedicineapiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicineapiService {
    private final MedicineapiRepository medicineapiRepository;

    public MedicineapiService(MedicineapiRepository medicineapiRepository) {
        this.medicineapiRepository = medicineapiRepository;
    }

    @Transactional(readOnly = true)
    public List<MedicineapiResponse> getAll() {
        return medicineapiRepository.findAll()
                .stream()
                .map(MedicineapiResponse::from)
                .toList();
    }
}