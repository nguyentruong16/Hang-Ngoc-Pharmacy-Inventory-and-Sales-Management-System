package com.example.project.service;

import com.example.project.dto.response.StatusResponse;
import com.example.project.repository.StatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StatusService {
    private final StatusRepository statusRepository;

    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    @Transactional(readOnly = true)
    public List<StatusResponse> getAll() {
        return statusRepository.findAll()
                .stream()
                .map(StatusResponse::from)
                .toList();
    }
}