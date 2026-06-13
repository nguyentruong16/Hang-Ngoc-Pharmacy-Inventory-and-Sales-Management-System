package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.entity.Account;
import com.example.project.entity.Accountpermission;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.AccountpermissionRepository;
import com.example.project.security.AccountPrincipal;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class CustomAccountDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final AccountpermissionRepository accountpermissionRepository;

    public CustomAccountDetailsService(
            AccountRepository accountRepository,
            AccountpermissionRepository accountpermissionRepository) {
        this.accountRepository = accountRepository;
        this.accountpermissionRepository = accountpermissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        String normalizedLoginId = loginId == null ? "" : loginId.trim();
        Account account = accountRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(normalizedLoginId, normalizedLoginId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (!Boolean.TRUE.equals(account.getStatus())) {
            throw new DisabledException("Account is inactive");
        }
        if (account.getPassword() == null || account.getPassword().isBlank()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        // Role + branch come from AccountPermission. Keep only rows whose role is one of the
        // known RoleConstants, ordered by accountPermissionID.
        // ASSUMPTION: if an account has several AccountPermission rows, the lowest-id valid row
        // defines the PRIMARY role and branch; every distinct valid role is still granted as an
        // authority (so a multi-role account can reach all of its role areas).
        List<Accountpermission> validRows = accountpermissionRepository.findByAccountId(account.getId())
                .stream()
                .filter(permission -> RoleConstants.isValid(canonicalRole(permission.getRole())))
                .sorted(Comparator.comparing(Accountpermission::getId))
                .toList();

        if (validRows.isEmpty()) {
            // Do NOT silently default to OWNER (or any role). An account with no valid role
            // assignment is not allowed to sign in — wrong navigation is worse than no access.
            throw new UsernameNotFoundException("Account has no valid role assignment");
        }

        Accountpermission primary = validRows.get(0);
        String primaryRole = canonicalRole(primary.getRole());
        Integer branchId = primary.getBranchID() == null ? null : primary.getBranchID().getId();

        List<GrantedAuthority> authorities = validRows.stream()
                .map(permission -> canonicalRole(permission.getRole()))
                .distinct()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .map(GrantedAuthority.class::cast)
                .toList();

        return new AccountPrincipal(
                account.getId(),
                account.getName(),
                account.getUsername(),
                account.getEmail(),
                account.getPassword(),
                true,
                authorities,
                primaryRole,
                branchId
        );
    }

    /**
     * Normalizes a stored role to its canonical bare form so it matches {@link RoleConstants}
     * (uppercased, with any leading {@code ROLE_} removed). Returns {@code null} for null input.
     */
    private String canonicalRole(String role) {
        if (role == null) {
            return null;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized.substring("ROLE_".length()) : normalized;
    }
}
