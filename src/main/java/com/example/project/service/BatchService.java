package com.example.project.service;

import com.example.project.entity.Batch;
import com.example.project.repository.BatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchService {
    private final BatchRepository batchRepository;

    public BatchService(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    public List<Batch> getAll() {
        return batchRepository.findAll();
    }
}