package com.example.project.service;

import com.example.project.dto.response.ProcurementplandetailResponse;
import com.example.project.repository.ProcurementplandetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProcurementplandetailService {
    private final ProcurementplandetailRepository procurementplandetailRepository;

    public ProcurementplandetailService(ProcurementplandetailRepository procurementplandetailRepository) {
        this.procurementplandetailRepository = procurementplandetailRepository;
    }

    @Transactional(readOnly = true)
    public List<ProcurementplandetailResponse> getAll() {
        return procurementplandetailRepository.findAll()
                .stream()
                .map(ProcurementplandetailResponse::from)
                .toList();
    }
}