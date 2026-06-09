package com.example.project.service;

import com.example.project.dto.response.ProducerResponse;
import com.example.project.repository.ProducerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProducerService {
    private final ProducerRepository producerRepository;

    public ProducerService(ProducerRepository producerRepository) {
        this.producerRepository = producerRepository;
    }

    @Transactional(readOnly = true)
    public List<ProducerResponse> getAll() {
        return producerRepository.findAll()
                .stream()
                .map(ProducerResponse::from)
                .toList();
    }
}