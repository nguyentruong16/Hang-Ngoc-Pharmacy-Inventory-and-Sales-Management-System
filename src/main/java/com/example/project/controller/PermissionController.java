package com.example.project.controller;

import com.example.project.constant.RoleConstants;
import com.example.project.service.OwnerPermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Owner-only "Bảng phân quyền" screen — a single store, so this is a flat account &rarr; role
 * table: one row per account, one role dropdown, one save button. Changing and saving a role posts
 * to {@code /owner/permissions/cell}, which assigns, updates or clears that one account's role and
 * bounces back to the same page/filters. Access is enforced by Spring Security
 * ({@code /owner/**} requires {@code ROLE_OWNER}), so no manual role check is needed here.
 */
@Controller
@RequestMapping("/owner/permissions")
public class PermissionController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    private final OwnerPermissionService ownerPermissionService;

    public PermissionController(OwnerPermissionService ownerPermissionService) {
        this.ownerPermissionService = ownerPermissionService;
    }

    @GetMapping
    public String view(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                       @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                       @RequestParam(name = "search", required = false) String search,
                       Model model) {
        model.addAttribute("permissionPage", ownerPermissionService.getPermissionPage(search, page, size));
        model.addAttribute("assignableRoleLabels", RoleConstants.permissionTableRoleLabels());
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Bảng phân quyền");
        return "owner/permissions";
    }

    @PostMapping("/cell")
    public String saveCell(@RequestParam Integer accountId,
                           @RequestParam(required = false) String role,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false, defaultValue = "0") int page,
                           @RequestParam(required = false, defaultValue = "10") int size,
                           RedirectAttributes redirectAttributes) {
        try {
            ownerPermissionService.saveRole(accountId, role);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phân quyền thành công");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        // Preserve the current page and search so the Owner stays where they were.
        redirectAttributes.addAttribute("page", page < 0 ? DEFAULT_PAGE : page);
        redirectAttributes.addAttribute("size", size <= 0 ? DEFAULT_SIZE : size);
        if (search != null && !search.isBlank()) {
            redirectAttributes.addAttribute("search", search);
        }

        return "redirect:/owner/permissions";
    }
}
