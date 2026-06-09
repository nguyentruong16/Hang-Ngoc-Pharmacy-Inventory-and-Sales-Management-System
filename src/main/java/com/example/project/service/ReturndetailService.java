package com.example.project.service;

import com.example.project.dto.response.ReturndetailResponse;
import com.example.project.repository.ReturndetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReturndetailService {
    private final ReturndetailRepository returndetailRepository;

    public ReturndetailService(ReturndetailRepository returndetailRepository) {
        this.returndetailRepository = returndetailRepository;
    }

    @Transactional(readOnly = true)
    public List<ReturndetailResponse> getAll() {
        return returndetailRepository.findAll()
                .stream()
                .map(ReturndetailResponse::from)
                .toList();
    }
}