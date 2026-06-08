package com.example.project.service;

import com.example.project.entity.Producer;
import com.example.project.repository.ProducerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProducerService {
    private final ProducerRepository producerRepository;

    public ProducerService(ProducerRepository producerRepository) {
        this.producerRepository = producerRepository;
    }

    public List<Producer> getAll() {
        return producerRepository.findAll();
    }
}