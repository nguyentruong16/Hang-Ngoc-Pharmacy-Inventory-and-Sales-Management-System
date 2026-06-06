package com.example.project.service;

import com.example.project.entity.Branch;
import com.example.project.repository.BranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {
    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public List<Branch> getAll() {
        return branchRepository.findAll();
    }
}
