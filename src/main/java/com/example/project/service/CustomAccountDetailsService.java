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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class CustomAccountDetailsService {
    private final AccountRepository accountRepository;
    private final AccountpermissionRepository accountpermissionRepository;

    public CustomAccountDetailsService(
            AccountRepository accountRepository,
            AccountpermissionRepository accountpermissionRepository) {
        this.accountRepository = accountRepository;
        this.accountpermissionRepository = accountpermissionRepository;
    }

    @Transactional(readOnly = true)
    public AccountPrincipal loadUserByLoginId(String loginId) throws UsernameNotFoundException {
        String normalizedLoginId = loginId == null ? "" : loginId.trim();
        if (normalizedLoginId.isBlank()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        Account account = accountRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(normalizedLoginId, normalizedLoginId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (!Boolean.TRUE.equals(account.getStatus())) {
            throw new DisabledException("Invalid credentials");
        }
        if (account.getPassword() == null || account.getPassword().isBlank()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        // Only the three roles (OWNER, PHARMACIST, ACCOUNTANT) may sign in.
        List<String> validRoles = accountpermissionRepository.findByAccountId(account.getId())
                .stream()
                .map(permission -> canonicalRole(permission.getRole()))
                .filter(RoleConstants::isValid)
                .toList();
        if (validRoles.isEmpty()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        // Owner wins when an account somehow holds several role rows.
        String selectedRole = validRoles.stream()
                .filter(RoleConstants.OWNER::equals)
                .findFirst()
                .orElse(validRoles.get(0));
        return buildPrincipal(account, selectedRole);
    }

    private AccountPrincipal buildPrincipal(Account account, String role) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        return new AccountPrincipal(
                account.getId(),
                account.getName(),
                account.getUsername(),
                account.getEmail(),
                account.getPassword(),
                true,
                authorities,
                role
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
