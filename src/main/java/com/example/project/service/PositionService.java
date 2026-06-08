package com.example.project.service;

import com.example.project.entity.Position;
import com.example.project.repository.PositionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionService {
    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<Position> getAll() {
        return positionRepository.findAll();
    }
}