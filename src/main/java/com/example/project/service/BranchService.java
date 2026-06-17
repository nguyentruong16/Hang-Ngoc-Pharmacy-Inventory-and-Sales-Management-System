package com.example.project.service;

import com.example.project.dto.request.BranchCreateRequest;
import com.example.project.dto.response.BranchResponse;
import com.example.project.entity.Branch;
import com.example.project.entity.Status;
import com.example.project.repository.BranchRepository;
import com.example.project.repository.StatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BranchService {
    private static final String ACTIVE_STATUS_NAME = "Đang hoạt động";

    private final BranchRepository branchRepository;
    private final StatusRepository statusRepository;

    public BranchService(BranchRepository branchRepository, StatusRepository statusRepository) {
        this.branchRepository = branchRepository;
        this.statusRepository = statusRepository;
    }

    //lay ra danh sach chi nhanh
    @Transactional(readOnly = true) //chi doc du lieu ko cho phep creat,update,...
    public List<BranchResponse> getAll() {
        return branchRepository.findAllWithStatus()
                .stream()
                .map(BranchResponse::from)
                .toList();
    }

    //dem so luong chi nhanh dang hoat dong
    public long countActiveBranches(List<BranchResponse> branches) {
        return branches.stream()
                .filter(branch -> ACTIVE_STATUS_NAME.equals(branch.getStatusName()))
                .count();
    }

    //lay ra chi tiet chi nhanh theo id
    @Transactional(readOnly = true)
    public BranchResponse getById(Integer id) {
        return BranchResponse.from(branchRepository.findByIdWithStatus(id).orElseThrow());
    }

    //tao chi nhanh
    @Transactional // tat ca thanh cong thi them ko thi rollback
    public BranchResponse create(BranchCreateRequest request) {

        //tao doi tuong status
        Status status = statusRepository.findByName(request.getStatusName())
                .orElseThrow(() -> new IllegalArgumentException("Trạng thái không hợp lệ"));

        //them chi nhanh vao db
        Branch branch = new Branch();
        branch.setName(request.getName().trim());
        branch.setAddress(request.getAddress().trim());
        branch.setStatusID(status);

        return BranchResponse.from(branchRepository.save(branch));
    }
}
