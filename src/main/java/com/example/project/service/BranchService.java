package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.dto.request.BranchCreateRequest;
import com.example.project.dto.response.BranchResponse;
import com.example.project.entity.Account;
import com.example.project.entity.Accountpermission;
import com.example.project.entity.Branch;
import com.example.project.entity.Status;
import com.example.project.repository.AccountpermissionRepository;
import com.example.project.repository.BranchRepository;
import com.example.project.repository.StatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BranchService {
    private static final String ACTIVE_STATUS_NAME = "Đang hoạt động";
    private static final String MSG_OWNER_NOT_FOUND =
            "Không tìm thấy tài khoản Chủ sở hữu để gán cho chi nhánh mới";

    private final BranchRepository branchRepository;
    private final StatusRepository statusRepository;
    private final AccountpermissionRepository accountpermissionRepository;

    public BranchService(BranchRepository branchRepository,
                         StatusRepository statusRepository,
                         AccountpermissionRepository accountpermissionRepository) {
        this.branchRepository = branchRepository;
        this.statusRepository = statusRepository;
        this.accountpermissionRepository = accountpermissionRepository;
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

        Branch saved = branchRepository.save(branch);

        // Business rule: the system Owner is Owner at every branch, so each new branch
        // automatically gets an OWNER assignment for the Owner account.
        assignOwnerToNewBranch(saved);

        return BranchResponse.from(saved);
    }

    /**
     * Gives the system Owner account an {@code OWNER} role at the freshly created branch, reusing
     * the existing {@code AccountPermission} table (no new tables). Idempotent: if the Owner is
     * already assigned to this branch, nothing happens. If no Owner account can be found, this
     * throws so the surrounding {@code @Transactional create(...)} rolls the branch back.
     */
    private void assignOwnerToNewBranch(Branch branch) {
        Account owner = accountpermissionRepository.findFirstOwnerPermission()
                .map(Accountpermission::getAccountID)
                .orElseThrow(() -> new IllegalStateException(MSG_OWNER_NOT_FOUND));

        if (owner == null || owner.getId() == null) {
            throw new IllegalStateException(MSG_OWNER_NOT_FOUND);
        }

        // Never create a duplicate Owner assignment for the same owner account + branch.
        if (accountpermissionRepository.existsAssignment(owner.getId(), branch.getId())) {
            return;
        }

        Accountpermission ownerPermission = new Accountpermission();
        // Manual id: accountpermission.accountPermissionID is not AUTO_INCREMENT in the schema.
        ownerPermission.setId(accountpermissionRepository.findMaxId() + 1);
        ownerPermission.setAccountID(owner);
        ownerPermission.setBranchID(branch);
        ownerPermission.setRole(RoleConstants.OWNER);
        accountpermissionRepository.save(ownerPermission);
    }

    //cap nhat chi nhanh
    @Transactional
    public BranchResponse update(Integer id, BranchCreateRequest request) {

        //lay chi nhanh can cap nhat
        Branch branch = branchRepository.findByIdWithStatus(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh"));

        //lay trang thai moi
        Status status = statusRepository.findByName(request.getStatusName())
                .orElseThrow(() -> new IllegalArgumentException("Trạng thái không hợp lệ"));

        //cap nhat thong tin chi nhanh
        branch.setName(request.getName().trim());
        branch.setAddress(request.getAddress().trim());
        branch.setStatusID(status);

        return BranchResponse.from(branchRepository.save(branch));
    }
}
