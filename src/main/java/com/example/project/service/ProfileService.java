package com.example.project.service;

import com.example.project.dto.request.ProfileUpdateRequest;
import com.example.project.dto.response.ProfileViewResponse;
import com.example.project.entity.Account;
import com.example.project.entity.Accountpermission;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.AccountpermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final AccountRepository accountRepository;
    private final AccountpermissionRepository accountpermissionRepository;

    public ProfileService(AccountRepository accountRepository,
                          AccountpermissionRepository accountpermissionRepository) {
        this.accountRepository = accountRepository;
        this.accountpermissionRepository = accountpermissionRepository;
    }

    @Transactional(readOnly = true)
    public ProfileViewResponse getProfile(Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        Accountpermission permission = accountpermissionRepository
                .findProfilePermissionByAccountId(accountId)
                .orElse(null);

        String role = permission != null ? formatRole(permission.getRole()) : "Chưa phân quyền";
        String branchName = permission != null && permission.getBranchID() != null
                ? permission.getBranchID().getName()
                : "Chưa có chi nhánh";

        return new ProfileViewResponse(
                account.getId(),
                account.getName(),
                account.getUsername(),
                account.getEmail(),
                account.getPhoneNumber(),
                role,
                branchName,
                account.getStatus()
        );
    }

    @Transactional
    public void updateProfile(Integer accountId, ProfileUpdateRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        account.setName(request.getName());
        account.setEmail(request.getEmail());
        account.setPhoneNumber(request.getPhoneNumber());

        accountRepository.save(account);
    }

    private String formatRole(String role) {
        if (role == null || role.isBlank()) {
            return "Chưa phân quyền";
        }

        return switch (role.toLowerCase()) {
            case "pharmacist", "duoc_si", "1" -> "Dược sĩ";
            case "chief_pharmacist", "duoc_si_truong", "2" -> "Dược sĩ trưởng";
            case "accountant", "ke_toan", "3" -> "Kế toán";
            case "cashier", "thu_ngan", "4" -> "Thu ngân";
            case "owner", "chu_nha_thuoc", "5" -> "Chủ nhà thuốc";
            default -> role;
        };
    }
}