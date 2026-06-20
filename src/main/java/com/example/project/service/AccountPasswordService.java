package com.example.project.service;

import com.example.project.entity.Account;
import com.example.project.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountPasswordService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountPasswordService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changePassword(Integer accountId, String currentPassword, String newPassword, String confirmPassword) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required.");
        }

        if (account.getPassword() == null || account.getPassword().isBlank()
                || !passwordEncoder.matches(currentPassword, account.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        validateNewPassword(newPassword, confirmPassword);
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void validateNewPassword(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required.");
        }
        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new IllegalArgumentException("Password confirmation is required.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }
    }
}
