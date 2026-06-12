package com.example.project.controller;

import com.example.project.security.AccountPrincipal;
import com.example.project.service.AccountPasswordService;
import com.example.project.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class AuthController {
    private static final String RESET_RESPONSE_MESSAGE = "If the email exists, a password reset link has been sent.";

    private final AccountPasswordService accountPasswordService;
    private final PasswordResetService passwordResetService;

    public AuthController(AccountPasswordService accountPasswordService, PasswordResetService passwordResetService) {
        this.accountPasswordService = accountPasswordService;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/signin")
    public String signin() {
        return "signin";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(
            @RequestParam(name = "email", required = false) String email,
            HttpServletRequest request,
            Model model) {
        passwordResetService.requestReset(email, resetBaseUrl(request));
        model.addAttribute("message", RESET_RESPONSE_MESSAGE);
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(name = "token", required = false) String token, Model model) {
        if (!passwordResetService.isTokenValid(token)) {
            model.addAttribute("error", "Password reset link is invalid or expired.");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam(name = "token", required = false) String token,
            @RequestParam(name = "newPassword", required = false) String newPassword,
            @RequestParam(name = "confirmPassword", required = false) String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.resetPassword(token, newPassword, confirmPassword);
            redirectAttributes.addAttribute("resetSuccess", "");
            return "redirect:/signin";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("token", token);
            model.addAttribute("error", exception.getMessage());
            return "reset-password";
        }
    }

    @GetMapping("/change-password")
    public String changePassword() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal AccountPrincipal principal,
            @RequestParam(name = "currentPassword", required = false) String currentPassword,
            @RequestParam(name = "newPassword", required = false) String newPassword,
            @RequestParam(name = "confirmPassword", required = false) String confirmPassword,
            Model model) {
        try {
            accountPasswordService.changePassword(principal.getAccountId(), currentPassword, newPassword, confirmPassword);
            return "redirect:/dashboard?passwordChanged";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            return "change-password";
        }
    }

    private String resetBaseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath() + "/reset-password")
                .replaceQuery(null)
                .build()
                .toUriString();
    }
}
