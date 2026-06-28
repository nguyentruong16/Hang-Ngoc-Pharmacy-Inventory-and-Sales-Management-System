package com.example.project.service;

import com.example.project.entity.Account;
import com.example.project.entity.PasswordResetToken;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.PasswordResetTokenRepository;
import com.example.project.security.AccountPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_BYTES = 32;
    private static final Duration RESET_REQUEST_COOLDOWN = Duration.ofSeconds(30);
    private static final String RESET_EMAIL_SUBJECT = "[Nhà thuốc Hằng Ngọc] Đặt lại mật khẩu tài khoản";
    private static final String RESET_EMAIL_SENDER_NAME = "Nhà thuốc Hằng Ngọc";

    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountPasswordService accountPasswordService;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SessionRegistry sessionRegistry;
    private final SecureRandom secureRandom = new SecureRandom();
    private final boolean logResetLink;
    private final String smtpHost;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String mailFrom;
    private final String authBaseUrl;

    public PasswordResetService(
            AccountRepository accountRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            AccountPasswordService accountPasswordService,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            SessionRegistry sessionRegistry,
            @Value("${app.auth.log-reset-link:true}") boolean logResetLink,
            @Value("${spring.mail.host:}") String smtpHost,
            @Value("${spring.mail.username:}") String smtpUsername,
            @Value("${spring.mail.password:}") String smtpPassword,
            @Value("${app.mail.from:}") String mailFrom,
            @Value("${app.auth.base-url:}") String authBaseUrl) {
        this.accountRepository = accountRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountPasswordService = accountPasswordService;
        this.mailSenderProvider = mailSenderProvider;
        this.sessionRegistry = sessionRegistry;
        this.logResetLink = logResetLink;
        this.smtpHost = smtpHost;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.mailFrom = mailFrom;
        this.authBaseUrl = authBaseUrl;
    }

    @Transactional
    public void requestReset(String email, String resetBaseUrl) {
        String normalizedEmail = email == null ? "" : email.trim();
        accountRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(account -> Boolean.TRUE.equals(account.getStatus()))
                .filter(this::isOutsideRequestCooldown)
                .ifPresent(account -> createAndDeliverResetLink(account, resetBaseUrl));
    }

    /**
     * Rate limiting: allow at most one reset request per account per 30 seconds. Prevents
     * mail-bombing a user's inbox and brute-forcing through repeated submissions. The caller
     * still returns a neutral message, so this never reveals whether the email exists.
     */
    private boolean isOutsideRequestCooldown(Account account) {
        LocalDateTime threshold = LocalDateTime.now().minus(RESET_REQUEST_COOLDOWN);
        boolean recentlyRequested =
                passwordResetTokenRepository.existsByAccountIDAndCreatedAtAfter(account, threshold);
        if (recentlyRequested) {
            log.info("Password reset request for account {} ignored: within {}s cooldown.",
                    account.getId(), RESET_REQUEST_COOLDOWN.toSeconds());
        }
        return !recentlyRequested;
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

        // Password changed via the email link: force every active session of this account to
        // re-authenticate, so a previously logged-in session (or a thief) cannot keep using it.
        invalidateActiveSessions(account);
    }

    /** Expires all tracked sessions belonging to the given account via the SessionRegistry. */
    private void invalidateActiveSessions(Account account) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof AccountPrincipal accountPrincipal
                    && Objects.equals(accountPrincipal.getAccountId(), account.getId())) {
                for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                    session.expireNow();
                }
            }
        }
    }

    private void createAndDeliverResetLink(Account account, String resetBaseUrl) {
        String rawToken = generateToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setAccountID(account);
        token.setTokenHash(hashToken(rawToken));
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.save(token);

        String resetUrl = buildResetUrl(resetBaseUrl, rawToken);
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
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    StandardCharsets.UTF_8.name());

            if (StringUtils.hasText(mailFrom)) {
                message.setFrom(new InternetAddress(
                        mailFrom.trim(),
                        RESET_EMAIL_SENDER_NAME,
                        StandardCharsets.UTF_8.name()));
            }
            message.setTo(account.getEmail());
            message.setSubject(RESET_EMAIL_SUBJECT);
            message.setText(resetEmailPlainText(resetUrl), resetEmailHtml(resetUrl));
            mailSender.send(mimeMessage);
            return true;
        } catch (MailException | MessagingException | UnsupportedEncodingException exception) {
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

    private String buildResetUrl(String requestResetBaseUrl, String rawToken) {
        String resetBaseUrl = requestResetBaseUrl;
        if (StringUtils.hasText(authBaseUrl)) {
            resetBaseUrl = authBaseUrl.trim().replaceAll("/+$", "") + "/reset-password";
        }
        return resetBaseUrl + "?token=" + rawToken;
    }

    private String resetEmailPlainText(String resetUrl) {
        return """
                Xin chào,

                Hệ thống nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.

                Vui lòng sử dụng liên kết bên dưới để đặt lại mật khẩu:
                %s

                Liên kết này sẽ hết hạn sau 30 phút.

                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này. Mật khẩu hiện tại của bạn sẽ không thay đổi.

                Đây là email tự động, vui lòng không trả lời email này.

                © 2026 Nhà thuốc Hằng Ngọc
                """.formatted(resetUrl);
    }

    private String resetEmailHtml(String resetUrl) {
        String escapedResetUrl = HtmlUtils.htmlEscape(resetUrl);
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Đặt lại mật khẩu</title>
                </head>
                <body style="margin:0;padding:0;background:#ecfdf5;font-family:Arial,'Helvetica Neue',Helvetica,sans-serif;color:#111827;">
                  <div style="width:100%%;background:#ecfdf5;padding:32px 12px;">
                    <div style="max-width:600px;margin:0 auto;background:#ffffff;border:1px solid #d1fae5;border-radius:18px;overflow:hidden;box-shadow:0 18px 48px rgba(15,23,42,0.10);">
                      <div style="background:linear-gradient(135deg,#059669 0%%,#047857 100%%);padding:28px 32px;color:#ffffff;">
                        <div style="font-size:24px;font-weight:800;line-height:1.2;">Nhà thuốc Hằng Ngọc</div>
                        <div style="font-size:14px;margin-top:6px;opacity:0.92;">Hệ thống quản lý nhà thuốc Hằng Ngọc</div>
                      </div>
                      <div style="padding:32px;">
                        <p style="margin:0 0 18px;font-size:16px;line-height:1.6;">Xin chào,</p>
                        <p style="margin:0 0 14px;font-size:15px;line-height:1.7;color:#374151;">Hệ thống nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                        <p style="margin:0 0 26px;font-size:15px;line-height:1.7;color:#374151;">Vui lòng nhấn vào nút bên dưới để đặt lại mật khẩu.</p>
                        <div style="text-align:center;margin:30px 0;">
                          <a href="%s" style="display:inline-block;background:#059669;color:#ffffff;text-decoration:none;font-weight:700;font-size:15px;padding:14px 26px;border-radius:12px;">Đặt lại mật khẩu</a>
                        </div>
                        <div style="background:#f0fdf4;border:1px solid #bbf7d0;border-radius:14px;padding:16px 18px;margin:0 0 22px;color:#065f46;font-size:14px;line-height:1.6;">
                          Liên kết này sẽ hết hạn sau 30 phút.
                        </div>
                        <p style="margin:0 0 18px;font-size:14px;line-height:1.7;color:#4b5563;">Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này. Mật khẩu hiện tại của bạn sẽ không thay đổi.</p>
                        <p style="margin:0;font-size:12px;line-height:1.6;color:#6b7280;">Đây là email tự động, vui lòng không trả lời email này.</p>
                      </div>
                      <div style="padding:18px 32px;background:#f8fafc;border-top:1px solid #e5e7eb;text-align:center;color:#64748b;font-size:12px;">
                        © 2026 Nhà thuốc Hằng Ngọc
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(escapedResetUrl);
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
