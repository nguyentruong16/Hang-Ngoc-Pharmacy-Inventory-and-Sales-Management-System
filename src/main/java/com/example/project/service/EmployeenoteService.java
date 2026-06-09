package com.example.project.service;

import com.example.project.dto.response.EmployeenoteResponse;
import com.example.project.repository.EmployeenoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeenoteService {
    private final EmployeenoteRepository employeenoteRepository;

    public EmployeenoteService(EmployeenoteRepository employeenoteRepository) {
        this.employeenoteRepository = employeenoteRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeenoteResponse> getAll() {
        return employeenoteRepository.findAll()
                .stream()
                .map(EmployeenoteResponse::from)
                .toList();
    }
}