package com.example.project.service;

import com.example.project.dto.response.BranchResponse;
import com.example.project.repository.BranchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BranchService {
    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getAll() {
        return branchRepository.findAllWithStatus()
                .stream()
                .map(BranchResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countActiveBranches() {
        return getAll().stream().filter(BranchResponse::isActive).count();
    }

    @Transactional(readOnly = true)
    public long countInactiveBranches() {
        return getAll().stream().filter(branch -> !branch.isActive()).count();
    }
}