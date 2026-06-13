package com.example.project.controller;

import com.example.project.dto.request.ProfileUpdateRequest;
import com.example.project.dto.response.ProfileViewResponse;
import com.example.project.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public String viewProfile(Model model) {
        Integer currentAccountId = getCurrentAccountIdForDemo();

        ProfileViewResponse profile = profileService.getProfile(currentAccountId);

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
                                Model model) {
        Integer currentAccountId = getCurrentAccountIdForDemo();

        if (bindingResult.hasErrors()) {
            ProfileViewResponse profile = profileService.getProfile(currentAccountId);
            model.addAttribute("profile", profile);
            return "profile";
        }

        profileService.updateProfile(currentAccountId, form);

        return "redirect:/profile?success";
    }

    private Integer getCurrentAccountIdForDemo() {
        // Tạm thời hard-code để demo khi chưa có Login/Spring Security.
        // Sau khi có đăng nhập thật, thay bằng accountId lấy từ session/principal.
        return 1;
    }
}