package com.example.project.service;

import com.example.project.entity.Return;
import com.example.project.repository.ReturnRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReturnService {
    private final ReturnRepository returnRepository;

    public ReturnService(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    public List<Return> getAll() {
        return returnRepository.findAll();
    }
}