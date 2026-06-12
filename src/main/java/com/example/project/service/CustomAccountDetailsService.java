package com.example.project.service;

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

import java.util.Collection;
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

        return new AccountPrincipal(
                account.getId(),
                account.getName(),
                account.getUsername(),
                account.getEmail(),
                account.getPassword(),
                true,
                authoritiesFor(account.getId())
        );
    }

    private Collection<? extends GrantedAuthority> authoritiesFor(Integer accountId) {
        List<GrantedAuthority> authorities = accountpermissionRepository.findByAccountId(accountId)
                .stream()
                .map(Accountpermission::getRole)
                .filter(role -> role != null && !role.isBlank())
                .map(this::normalizeRole)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        if (authorities.isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return authorities;
    }

    private String normalizeRole(String role) {
        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        if (normalizedRole.startsWith("ROLE_")) {
            return normalizedRole;
        }
        return "ROLE_" + normalizedRole;
    }
}
