package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.dto.request.OwnerUserCreateRequest;
import com.example.project.dto.response.OwnerUserRowResponse;
import com.example.project.dto.response.OwnerUserStatsResponse;
import com.example.project.entity.Account;
import com.example.project.entity.Accountpermission;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.AccountpermissionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OwnerUserService {

    private final AccountRepository accountRepository;
    private final AccountpermissionRepository accountpermissionRepository;
    private final PasswordEncoder passwordEncoder;

    public OwnerUserService(AccountRepository accountRepository,
                            AccountpermissionRepository accountpermissionRepository,
                            PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.accountpermissionRepository = accountpermissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<OwnerUserRowResponse> listUsers(String search, String role, String status, Pageable pageable) {
        final String keyword = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        final String roleFilter = RoleConstants.isValid(role) ? role : null;
        final Boolean statusFilter = resolveStatusFilter(status);

        List<Accountpermission> permissions = accountpermissionRepository.findAllWithAccountAndBranch();

        Map<Integer, List<Accountpermission>> permissionMap = permissions.stream()
                .filter(ap -> ap.getAccountID() != null)
                .collect(Collectors.groupingBy(ap -> ap.getAccountID().getId()));

        List<OwnerUserRowResponse> filteredUsers = accountRepository.findAll()
                .stream()
                .filter(account -> matchesSearch(
                        account,
                        permissionMap.getOrDefault(account.getId(), List.of()),
                        keyword
                ))
                .filter(account -> statusFilter == null
                        || statusFilter.equals(Boolean.TRUE.equals(account.getStatus())))
                .filter(account -> roleFilter == null
                        || hasRole(permissionMap.get(account.getId()), roleFilter))
                .sorted(Comparator.comparing(Account::getId))
                .map(account -> toRow(account, permissionMap.getOrDefault(account.getId(), List.of())))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredUsers.size());

        List<OwnerUserRowResponse> pageContent;

        if (start > filteredUsers.size()) {
            pageContent = List.of();
        } else {
            pageContent = filteredUsers.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, filteredUsers.size());
    }

    private Boolean resolveStatusFilter(String status) {
        if ("active".equalsIgnoreCase(status)) {
            return true;
        }

        if ("inactive".equalsIgnoreCase(status)) {
            return false;
        }

        return null;
    }

    @Transactional(readOnly = true)
    public OwnerUserStatsResponse getStats() {
        List<Account> accounts = accountRepository.findAll();

        List<Accountpermission> permissions = accountpermissionRepository.findAllWithAccountAndBranch();

        Set<Integer> pharmacistAccountIds = permissions.stream()
                .filter(ap -> ap.getAccountID() != null)
                .filter(ap -> RoleConstants.PHARMACIST.equals(ap.getRole()))
                .map(ap -> ap.getAccountID().getId())
                .collect(Collectors.toSet());

        Set<Integer> accountantAccountIds = permissions.stream()
                .filter(ap -> ap.getAccountID() != null)
                .filter(ap -> RoleConstants.ACCOUNTANT.equals(ap.getRole()))
                .map(ap -> ap.getAccountID().getId())
                .collect(Collectors.toSet());

        long total = accounts.size();
        long active = accounts.stream()
                .filter(account -> Boolean.TRUE.equals(account.getStatus()))
                .count();
        long inactive = total - active;

        return new OwnerUserStatsResponse(
                total,
                active,
                inactive,
                pharmacistAccountIds.size(),
                accountantAccountIds.size()
        );
    }

    @Transactional
    public void createUser(OwnerUserCreateRequest request) {
        validateCreateRequest(request);

        Account account = new Account();
        account.setName(request.getName().trim());
        account.setUsername(request.getUsername().trim());
        account.setEmail(request.getEmail().trim());
        account.setPhoneNumber(request.getPhoneNumber().trim());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setStatus(Boolean.TRUE.equals(request.getStatus()));

        Account savedAccount = accountRepository.save(account);

        Accountpermission permission = new Accountpermission();
        permission.setId(accountpermissionRepository.findMaxId() + 1);
        permission.setAccountID(savedAccount);
        permission.setRole(request.getRole());

        accountpermissionRepository.save(permission);
    }

    private void validateCreateRequest(OwnerUserCreateRequest request) {
        if (!RoleConstants.isValid(request.getRole())) {
            throw new IllegalArgumentException("Vai trò không hợp lệ");
        }

        if (RoleConstants.OWNER.equals(request.getRole())) {
            throw new IllegalArgumentException("Không được tạo thêm tài khoản Chủ sở hữu");
        }

        if (accountRepository.existsByUsernameIgnoreCase(request.getUsername().trim())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        if (accountRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
    }

    private boolean matchesSearch(Account account,
                                  List<Accountpermission> permissions,
                                  String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String normalizedKeyword = normalizeText(keyword);

        boolean matchAccountInfo =
                containsNormalized(account.getName(), normalizedKeyword)
                        || containsNormalized(account.getUsername(), normalizedKeyword)
                        || containsNormalized(account.getEmail(), normalizedKeyword)
                        || containsNormalized(account.getPhoneNumber(), normalizedKeyword)
                        || containsNormalized(formatEmployeeCode(account.getId()), normalizedKeyword);

        if (matchAccountInfo) {
            return true;
        }

        boolean matchRole = permissions != null && permissions.stream()
                .map(Accountpermission::getRole)
                .filter(Objects::nonNull)
                .map(RoleConstants::vietnameseName)
                .anyMatch(roleName -> containsNormalized(roleName, normalizedKeyword));

        if (matchRole) {
            return true;
        }

        return false;
    }

    private boolean containsNormalized(String value, String normalizedKeyword) {
        return value != null && normalizeText(value).contains(normalizedKeyword);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replace("Đ", "D").replace("đ", "d");

        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private boolean hasRole(List<Accountpermission> permissions, String role) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        return permissions.stream()
                .map(Accountpermission::getRole)
                .filter(Objects::nonNull)
                .anyMatch(userRole -> role.equals(userRole));
    }

    private OwnerUserRowResponse toRow(Account account, List<Accountpermission> permissions) {
        String roleDisplay = permissions.stream()
                .map(Accountpermission::getRole)
                .filter(Objects::nonNull)
                .distinct()
                .map(RoleConstants::vietnameseName)
                .collect(Collectors.joining(", "));

        if (roleDisplay.isBlank()) {
            roleDisplay = "Chưa phân quyền";
        }

        String branchNames = "Toàn hệ thống";

        boolean active = Boolean.TRUE.equals(account.getStatus());

        boolean isOwner = permissions.stream()
                .map(Accountpermission::getRole)
                .filter(Objects::nonNull)
                .anyMatch(RoleConstants.OWNER::equals);

        boolean canDeactivate = active && !isOwner;
        boolean canActivate = !active && !isOwner;

        return new OwnerUserRowResponse(
                account.getId(),
                formatEmployeeCode(account.getId()),
                account.getName(),
                account.getUsername(),
                roleDisplay,
                branchNames,
                account.getPhoneNumber(),
                account.getEmail(),
                active,
                active ? "Đang hoạt động" : "Ngừng hoạt động",
                canDeactivate,
                canActivate
        );
    }

    private String formatEmployeeCode(Integer accountId) {
        if (accountId == null) {
            return "ACC-000000";
        }
        return "ACC-" + String.format("%06d", accountId);
    }

    @Transactional
    public void deactivateUser(Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        List<Accountpermission> permissions = accountpermissionRepository.findByAccountId(accountId);

        boolean isOwner = permissions.stream()
                .anyMatch(permission -> RoleConstants.OWNER.equals(permission.getRole()));

        if (isOwner) {
            throw new IllegalArgumentException("Không thể vô hiệu hóa tài khoản Chủ sở hữu");
        }

        if (!Boolean.TRUE.equals(account.getStatus())) {
            throw new IllegalArgumentException("Tài khoản này đã bị vô hiệu hóa");
        }

        account.setStatus(false);
        accountRepository.save(account);
    }

    @Transactional
    public void activateUser(Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        List<Accountpermission> permissions = accountpermissionRepository.findByAccountId(accountId);

        boolean isOwner = permissions.stream()
                .anyMatch(permission -> RoleConstants.OWNER.equals(permission.getRole()));

        if (isOwner) {
            throw new IllegalArgumentException("Không thể thao tác trạng thái tài khoản Chủ sở hữu");
        }

        if (Boolean.TRUE.equals(account.getStatus())) {
            throw new IllegalArgumentException("Tài khoản này đang hoạt động");
        }

        account.setStatus(true);
        accountRepository.save(account);
    }
}