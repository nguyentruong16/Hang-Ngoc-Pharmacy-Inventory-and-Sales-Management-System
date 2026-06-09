package com.example.project.service;

import com.example.project.dto.response.ProcurementplanResponse;
import com.example.project.repository.ProcurementplanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProcurementplanService {
    private final ProcurementplanRepository procurementplanRepository;

    public ProcurementplanService(ProcurementplanRepository procurementplanRepository) {
        this.procurementplanRepository = procurementplanRepository;
    }

    @Transactional(readOnly = true)
    public List<ProcurementplanResponse> getAll() {
        return procurementplanRepository.findAll()
                .stream()
                .map(ProcurementplanResponse::from)
                .toList();
    }
}