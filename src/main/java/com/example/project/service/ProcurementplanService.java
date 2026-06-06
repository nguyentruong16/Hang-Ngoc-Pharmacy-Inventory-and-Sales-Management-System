package com.example.project.service;

import com.example.project.entity.Procurementplan;
import com.example.project.repository.ProcurementplanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcurementplanService {
    private final ProcurementplanRepository procurementplanRepository;

    public ProcurementplanService(ProcurementplanRepository procurementplanRepository) {
        this.procurementplanRepository = procurementplanRepository;
    }

    public List<Procurementplan> getAll() {
        return procurementplanRepository.findAll();
    }
}
