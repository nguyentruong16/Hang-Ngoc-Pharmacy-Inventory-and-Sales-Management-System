package com.example.project.service;

import com.example.project.entity.Account;
import com.example.project.entity.PasswordResetToken;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_BYTES = 32;

    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountPasswordService accountPasswordService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final boolean logResetLink;

    public PasswordResetService(
            AccountRepository accountRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            AccountPasswordService accountPasswordService,
            @Value("${app.auth.log-reset-link:true}") boolean logResetLink) {
        this.accountRepository = accountRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountPasswordService = accountPasswordService;
        this.logResetLink = logResetLink;
    }

    @Transactional
    public void requestReset(String email, String resetBaseUrl) {
        String normalizedEmail = email == null ? "" : email.trim();
        accountRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(account -> Boolean.TRUE.equals(account.getStatus()))
                .ifPresent(account -> createAndLogResetLink(account, resetBaseUrl));
    }

    @Transactional(readOnly = true)
    public boolean isTokenValid(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }
        return passwordResetTokenRepository.findByTokenHash(hashToken(rawToken))
                .filter(this::isUsable)
                .isPresent();
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword, String confirmPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Password reset link is invalid or expired.");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hashToken(rawToken))
                .filter(this::isUsable)
                .orElseThrow(() -> new IllegalArgumentException("Password reset link is invalid or expired."));

        accountPasswordService.validateNewPassword(newPassword, confirmPassword);
        Account account = token.getAccountID();
        account.setPassword(passwordEncoder.encode(newPassword));
        token.setUsedAt(LocalDateTime.now());
        accountRepository.save(account);
        passwordResetTokenRepository.save(token);
    }

    private void createAndLogResetLink(Account account, String resetBaseUrl) {
        String rawToken = generateToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setAccountID(account);
        token.setTokenHash(hashToken(rawToken));
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.save(token);

        if (logResetLink) {
            log.info("Local development password reset URL for account {}: {}?token={}",
                    account.getId(), resetBaseUrl, rawToken);
        }
    }

    private boolean isUsable(PasswordResetToken token) {
        return token.getUsedAt() == null && token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private String generateToken() {
        byte[] token = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.trim().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }
}
