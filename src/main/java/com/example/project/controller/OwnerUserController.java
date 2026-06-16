package com.example.project.controller;

import com.example.project.constant.RoleConstants;
import com.example.project.dto.request.OwnerUserCreateRequest;
import com.example.project.service.OwnerUserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.project.dto.response.OwnerUserRowResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/owner/users")
public class OwnerUserController {

    private final OwnerUserService ownerUserService;

    public OwnerUserController(OwnerUserService ownerUserService) {
        this.ownerUserService = ownerUserService;
    }

    @GetMapping
    public String listUsers(@RequestParam(name = "search", required = false) String search,
                            @RequestParam(name = "role", required = false) String role,
                            @RequestParam(name = "status", required = false) String status,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "5") int size,
                            Model model) {

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<OwnerUserRowResponse> userPage =
                ownerUserService.listUsers(search, role, status, pageable);

        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());

        model.addAttribute("stats", ownerUserService.getStats());
        model.addAttribute("roleLabels", RoleConstants.vietnameseLabels());

        model.addAttribute("search", search);
        model.addAttribute("filterRole", role);
        model.addAttribute("filterStatus", status);

        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", userPage.getTotalElements());

        return "owner/users";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new OwnerUserCreateRequest());
        }

        addCreatePageData(model);
        return "owner/user-create";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("form") OwnerUserCreateRequest form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addCreatePageData(model);
            return "owner/user-create";
        }

        try {
            ownerUserService.createUser(form);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo người dùng mới thành công");
            return "redirect:/owner/users";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            addCreatePageData(model);
            return "owner/user-create";
        }
    }

    @PostMapping("/{accountId}/deactivate")
    public String deactivateUser(@PathVariable Integer accountId,
                                 RedirectAttributes redirectAttributes) {
        try {
            ownerUserService.deactivateUser(accountId);
            redirectAttributes.addFlashAttribute("successMessage", "Vô hiệu hóa tài khoản thành công");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/owner/users";
    }

    @PostMapping("/{accountId}/activate")
    public String activateUser(@PathVariable Integer accountId,
                               RedirectAttributes redirectAttributes) {
        try {
            ownerUserService.activateUser(accountId);
            redirectAttributes.addFlashAttribute("successMessage", "Kích hoạt lại tài khoản thành công");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/owner/users";
    }

    private void addCreatePageData(Model model) {
        model.addAttribute("roleLabels", RoleConstants.nonOwnerVietnameseLabels());
        model.addAttribute("branches", ownerUserService.listBranches());
    }
}