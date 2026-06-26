package com.example.project.service;

import com.example.project.dto.request.OriginCreateRequest;
import com.example.project.dto.response.OriginResponse;
import com.example.project.entity.Origin;
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

    @Transactional
    public OriginResponse create(OriginCreateRequest request) {
        Origin origin = new Origin();
        origin.setName(request.getName().trim());
        return OriginResponse.from(originRepository.save(origin));
    }
}