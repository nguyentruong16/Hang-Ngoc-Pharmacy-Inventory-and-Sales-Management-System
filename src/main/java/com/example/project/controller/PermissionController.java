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
 * Owner-only "Bảng phân quyền" (permission matrix) screen.
 *
 * <p>Shows one row per account and one column per branch; each cell is a role dropdown backed by
 * real {@code AccountPermission} rows via {@link OwnerPermissionService}. Changing a dropdown
 * posts to {@code /owner/permissions/cell}, which assigns, updates or clears that single role.
 * Access is enforced by Spring Security ({@code /owner/**} requires {@code ROLE_OWNER}), so no
 * manual role check is needed here.</p>
 */
@Controller
@RequestMapping("/owner/permissions")
public class PermissionController {

    private final OwnerPermissionService ownerPermissionService;

    public PermissionController(OwnerPermissionService ownerPermissionService) {
        this.ownerPermissionService = ownerPermissionService;
    }

    @GetMapping
    public String view(@RequestParam(name = "search", required = false) String search,
                       @RequestParam(name = "roleFilter", required = false) String roleFilter,
                       Model model) {
        model.addAttribute("matrixRows", ownerPermissionService.listMatrixRows(search, roleFilter));
        model.addAttribute("branches", ownerPermissionService.listBranches());
        model.addAttribute("assignableRoleLabels", RoleConstants.nonOwnerVietnameseLabels());
        model.addAttribute("roleLabels", RoleConstants.vietnameseLabels());
        model.addAttribute("search", search);
        model.addAttribute("roleFilter", roleFilter);
        model.addAttribute("pageTitle", "Bảng phân quyền");
        return "owner/permissions";
    }

    @PostMapping("/cell")
    public String saveCell(@RequestParam Integer accountId,
                           @RequestParam Integer branchId,
                           @RequestParam(required = false) String role,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) String roleFilter,
                           RedirectAttributes redirectAttributes) {
        try {
            ownerPermissionService.saveMatrixCell(accountId, branchId, role);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phân quyền thành công");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        if (search != null && !search.isBlank()) {
            redirectAttributes.addAttribute("search", search);
        }
        if (roleFilter != null && !roleFilter.isBlank()) {
            redirectAttributes.addAttribute("roleFilter", roleFilter);
        }

        return "redirect:/owner/permissions";
    }
}
