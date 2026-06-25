package com.example.project.service;

import com.example.project.dto.request.ProfileUpdateRequest;
import com.example.project.dto.response.ProfileViewResponse;
import com.example.project.entity.Account;
import com.example.project.entity.Accountpermission;
import com.example.project.entity.Branch;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.AccountpermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

        List<Accountpermission> permissions =
                accountpermissionRepository.findProfilePermissionsByAccountId(accountId);

        String role = permissions.stream()
                .map(Accountpermission::getRole)
                .filter(roleValue -> roleValue != null && !roleValue.isBlank())
                .map(this::formatRole)
                .distinct()
                .collect(Collectors.joining(", "));

        if (role.isBlank()) {
            role = "Chưa phân quyền";
        }

        String branchName = permissions.stream()
                .map(Accountpermission::getBranchID)
                .filter(branch -> branch != null)
                .map(Branch::getName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        if (branchName.isBlank()) {
            branchName = "Chưa có chi nhánh";
        }

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
        account.setPhoneNumber(request.getPhoneNumber().trim());

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
            case "owner", "chu_nha_thuoc", "5" -> "Chủ nhà thuốc";
            default -> role;
        };
    }
}