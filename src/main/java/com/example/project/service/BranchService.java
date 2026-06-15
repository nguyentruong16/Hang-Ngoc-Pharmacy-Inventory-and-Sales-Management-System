package com.example.project.service;

import com.example.project.dto.response.BranchResponse;
import com.example.project.repository.BranchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BranchService {
    private static final String ACTIVE_STATUS_NAME = "Đang hoạt động";

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

    public long countActiveBranches(List<BranchResponse> branches) {
        return branches.stream()
                .filter(branch -> ACTIVE_STATUS_NAME.equals(branch.getStatusName()))
                .count();
    }
}
