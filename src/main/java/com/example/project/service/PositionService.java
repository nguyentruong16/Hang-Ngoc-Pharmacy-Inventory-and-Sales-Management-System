package com.example.project.service;

import com.example.project.dto.response.PositionResponse;
import com.example.project.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PositionService {
    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    @Transactional(readOnly = true)
    public List<PositionResponse> getAll() {
        return positionRepository.findAll()
                .stream()
                .map(PositionResponse::from)
                .toList();
    }
}