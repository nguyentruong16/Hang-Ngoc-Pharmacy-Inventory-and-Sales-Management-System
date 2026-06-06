package com.example.project.service;

import com.example.project.entity.Procurementplandetail;
import com.example.project.repository.ProcurementplandetailRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcurementplandetailService {
    private final ProcurementplandetailRepository procurementplandetailRepository;

    public ProcurementplandetailService(ProcurementplandetailRepository procurementplandetailRepository) {
        this.procurementplandetailRepository = procurementplandetailRepository;
    }

    public List<Procurementplandetail> getAll() {
        return procurementplandetailRepository.findAll();
    }
}
