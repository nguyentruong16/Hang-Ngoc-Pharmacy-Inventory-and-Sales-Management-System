package com.example.project.service;

import com.example.project.entity.Origin;
import com.example.project.repository.OriginRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OriginService {
    private final OriginRepository originRepository;

    public OriginService(OriginRepository originRepository) {
        this.originRepository = originRepository;
    }

    public List<Origin> getAll() {
        return originRepository.findAll();
    }
}