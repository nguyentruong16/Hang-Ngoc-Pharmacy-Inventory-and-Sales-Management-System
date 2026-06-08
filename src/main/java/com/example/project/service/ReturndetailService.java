package com.example.project.service;

import com.example.project.entity.Returndetail;
import com.example.project.repository.ReturndetailRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReturndetailService {
    private final ReturndetailRepository returndetailRepository;

    public ReturndetailService(ReturndetailRepository returndetailRepository) {
        this.returndetailRepository = returndetailRepository;
    }

    public List<Returndetail> getAll() {
        return returndetailRepository.findAll();
    }
}