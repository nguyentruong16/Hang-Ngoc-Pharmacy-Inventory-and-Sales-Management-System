package com.example.project.service;

import com.example.project.entity.Account;
import com.example.project.entity.PasswordResetToken;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SecureRandom secureRandom = new SecureRandom();
    private final boolean logResetLink;
    private final String smtpHost;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String mailFrom;

    public PasswordResetService(
            AccountRepository accountRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            AccountPasswordService accountPasswordService,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.auth.log-reset-link:true}") boolean logResetLink,
            @Value("${spring.mail.host:}") String smtpHost,
            @Value("${spring.mail.username:}") String smtpUsername,
            @Value("${spring.mail.password:}") String smtpPassword,
            @Value("${app.mail.from:}") String mailFrom) {
        this.accountRepository = accountRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountPasswordService = accountPasswordService;
        this.mailSenderProvider = mailSenderProvider;
        this.logResetLink = logResetLink;
        this.smtpHost = smtpHost;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.mailFrom = mailFrom;
    }

    @Transactional
    public void requestReset(String email, String resetBaseUrl) {
        String normalizedEmail = email == null ? "" : email.trim();
        accountRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(account -> Boolean.TRUE.equals(account.getStatus()))
                .ifPresent(account -> createAndDeliverResetLink(account, resetBaseUrl));
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

    private void createAndDeliverResetLink(Account account, String resetBaseUrl) {
        String rawToken = generateToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setAccountID(account);
        token.setTokenHash(hashToken(rawToken));
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.save(token);

        String resetUrl = resetBaseUrl + "?token=" + rawToken;
        if (sendResetEmail(account, resetUrl)) {
            return;
        }
        logLocalResetUrl(account, resetUrl);
    }

    private boolean sendResetEmail(Account account, String resetUrl) {
        if (!isMailConfigured()) {
            return false;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(mailFrom)) {
                message.setFrom(mailFrom.trim());
            }
            message.setTo(account.getEmail());
            message.setSubject("Pharmacy Management System password reset");
            message.setText("""
                    A password reset was requested for your Pharmacy Management System staff account.

                    Use this link to reset your password:
                    %s

                    This link expires in 30 minutes.

                    If you did not request a password reset, ignore this email.
                    """.formatted(resetUrl));
            mailSender.send(message);
            return true;
        } catch (MailException exception) {
            log.warn("Password reset email could not be sent for account {}. Falling back to local reset URL logging.",
                    account.getId());
            return false;
        }
    }

    private boolean isMailConfigured() {
        return StringUtils.hasText(smtpHost)
                && StringUtils.hasText(smtpUsername)
                && StringUtils.hasText(smtpPassword);
    }

    private void logLocalResetUrl(Account account, String resetUrl) {
        if (logResetLink) {
            log.info("Local development password reset URL for account {}: {}", account.getId(), resetUrl);
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
