package com.example.project.controller;

import com.example.project.dto.request.ProfileUpdateRequest;
import com.example.project.dto.response.ProfileViewResponse;
import com.example.project.entity.Account;
import com.example.project.repository.AccountRepository;
import com.example.project.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final AccountRepository accountRepository;

    public ProfileController(ProfileService profileService,
                             AccountRepository accountRepository) {
        this.profileService = profileService;
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public String viewProfile(Model model, Authentication authentication) {
        Account currentAccount = getCurrentAccount(authentication);

        ProfileViewResponse profile = profileService.getProfile(currentAccount.getId());

        ProfileUpdateRequest form = new ProfileUpdateRequest();
        form.setName(profile.getName());
        form.setEmail(profile.getEmail());
        form.setPhoneNumber(profile.getPhoneNumber());

        model.addAttribute("profile", profile);
        model.addAttribute("profileForm", form);

        return "profile";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileUpdateRequest form,
                                BindingResult bindingResult,
                                Model model,
                                Authentication authentication) {
        Account currentAccount = getCurrentAccount(authentication);

        if (bindingResult.hasErrors()) {
            ProfileViewResponse profile = profileService.getProfile(currentAccount.getId());
            model.addAttribute("profile", profile);
            return "profile";
        }

        profileService.updateProfile(currentAccount.getId(), form);

        return "redirect:/profile?success";
    }

    private Account getCurrentAccount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }

        String loginValue = authentication.getName();

        return accountRepository.findByUsernameOrEmail(loginValue, loginValue)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản đang đăng nhập: " + loginValue));
    }
}