package com.example.project.service;

import com.example.project.entity.Employeenote;
import com.example.project.repository.EmployeenoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeenoteService {
    private final EmployeenoteRepository employeenoteRepository;

    public EmployeenoteService(EmployeenoteRepository employeenoteRepository) {
        this.employeenoteRepository = employeenoteRepository;
    }

    public List<Employeenote> getAll() {
        return employeenoteRepository.findAll();
    }
}