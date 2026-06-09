package com.example.project.service;

import com.example.project.dto.response.OriginResponse;
import com.example.project.repository.OriginRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OriginService {
    private final OriginRepository originRepository;

    public OriginService(OriginRepository originRepository) {
        this.originRepository = originRepository;
    }

    @Transactional(readOnly = true)
    public List<OriginResponse> getAll() {
        return originRepository.findAll()
                .stream()
                .map(OriginResponse::from)
                .toList();
    }
}