package com.example.project.service;

import com.example.project.entity.Status;
import com.example.project.repository.StatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusService {
    private final StatusRepository statusRepository;

    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public List<Status> getAll() {
        return statusRepository.findAll();
    }
}
