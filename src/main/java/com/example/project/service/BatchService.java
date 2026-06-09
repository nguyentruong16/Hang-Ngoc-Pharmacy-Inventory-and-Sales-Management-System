package com.example.project.service;

import com.example.project.dto.response.BatchResponse;
import com.example.project.repository.BatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BatchService {
    private final BatchRepository batchRepository;

    public BatchService(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getAll() {
        return batchRepository.findAll()
                .stream()
                .map(BatchResponse::from)
                .toList();
    }
}