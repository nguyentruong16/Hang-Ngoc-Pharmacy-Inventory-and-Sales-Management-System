package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.entity.Account;
import com.example.project.entity.Accountpermission;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.AccountpermissionRepository;
import com.example.project.repository.BranchRepository;
import com.example.project.security.AccountPrincipal;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class CustomAccountDetailsService {
    private final AccountRepository accountRepository;
    private final AccountpermissionRepository accountpermissionRepository;
    private final BranchRepository branchRepository;

    public CustomAccountDetailsService(
            AccountRepository accountRepository,
            AccountpermissionRepository accountpermissionRepository,
            BranchRepository branchRepository) {
        this.accountRepository = accountRepository;
        this.accountpermissionRepository = accountpermissionRepository;
        this.branchRepository = branchRepository;
    }

    @Transactional(readOnly = true)
    public AccountPrincipal loadUserByUsernameAndBranch(String loginId, Integer branchId) throws UsernameNotFoundException {
        String normalizedLoginId = loginId == null ? "" : loginId.trim();
        if (normalizedLoginId.isBlank()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        // Credential check first — branch is NOT needed to identify/authenticate the account.
        Account account = accountRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(normalizedLoginId, normalizedLoginId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (!Boolean.TRUE.equals(account.getStatus())) {
            throw new DisabledException("Invalid credentials");
        }
        if (account.getPassword() == null || account.getPassword().isBlank()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        // All valid role rows for the account (across every branch).
        List<Accountpermission> validRows = accountpermissionRepository.findProfilePermissionsByAccountId(account.getId())
                .stream()
                .filter(permission -> RoleConstants.isValid(canonicalRole(permission.getRole())))
                .toList();
        if (validRows.isEmpty()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        // Owner is cross-branch: no branch selection required; branchId stays null = all branches.
        boolean isOwner = validRows.stream()
                .anyMatch(permission -> RoleConstants.OWNER.equals(canonicalRole(permission.getRole())));
        if (isOwner) {
            return buildPrincipal(account, RoleConstants.OWNER, null);
        }

        // Non-owner roles are branch-scoped: a valid branch must be chosen and matched.
        if (branchId == null) {
            throw new UsernameNotFoundException("Invalid credentials");
        }
        branchRepository.findById(branchId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        List<Accountpermission> branchRows = accountpermissionRepository.findByAccountIdAndBranchId(account.getId(), branchId)
                .stream()
                .filter(permission -> RoleConstants.isValid(canonicalRole(permission.getRole())))
                .toList();
        if (branchRows.isEmpty()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        Accountpermission selectedPermission = branchRows.get(0);
        String selectedRole = canonicalRole(selectedPermission.getRole());
        Integer selectedBranchId = selectedPermission.getBranchID() == null
                ? branchId
                : selectedPermission.getBranchID().getId();
        return buildPrincipal(account, selectedRole, selectedBranchId);
    }

    private AccountPrincipal buildPrincipal(Account account, String role, Integer branchId) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        return new AccountPrincipal(
                account.getId(),
                account.getName(),
                account.getUsername(),
                account.getEmail(),
                account.getPassword(),
                true,
                authorities,
                role,
                branchId
        );
    }

    private String canonicalRole(String role) {
        if (role == null) {
            return null;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized.substring("ROLE_".length()) : normalized;
    }
}
