package com.example.project.service;

import com.example.project.dto.response.ReturnResponse;
import com.example.project.repository.ReturnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReturnService {
    private final ReturnRepository returnRepository;

    public ReturnService(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> getAll() {
        return returnRepository.findAll()
                .stream()
                .map(ReturnResponse::from)
                .toList();
    }
}