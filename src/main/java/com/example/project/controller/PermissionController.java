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
 * Owner-only "Bảng phân quyền" screen.
 *
 * <p>The Owner selects a branch via the branch buttons and sees a paginated table of accounts
 * with their role <em>at that branch</em>, backed by real {@code AccountPermission} rows via
 * {@link OwnerPermissionService}. Changing a role dropdown posts to
 * {@code /owner/permissions/cell}, which assigns, updates or clears that one role and bounces
 * back to the same branch/page/filters. Access is enforced by Spring Security
 * ({@code /owner/**} requires {@code ROLE_OWNER}), so no manual role check is needed here.</p>
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
    public String view(@RequestParam(name = "branchId", required = false) Integer branchId,
                       @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                       @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                       @RequestParam(name = "search", required = false) String search,
                       @RequestParam(name = "roleFilter", required = false) String roleFilter,
                       Model model) {
        model.addAttribute("permissionPage",
                ownerPermissionService.getPermissionPage(branchId, search, roleFilter, page, size));
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
                           @RequestParam(required = false, defaultValue = "0") int page,
                           @RequestParam(required = false, defaultValue = "10") int size,
                           RedirectAttributes redirectAttributes) {
        try {
            ownerPermissionService.saveBranchRole(accountId, branchId, role);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phân quyền thành công");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        // Preserve the selected branch, page and filters so the Owner stays where they were.
        redirectAttributes.addAttribute("branchId", branchId);
        redirectAttributes.addAttribute("page", page < 0 ? DEFAULT_PAGE : page);
        redirectAttributes.addAttribute("size", size <= 0 ? DEFAULT_SIZE : size);
        if (search != null && !search.isBlank()) {
            redirectAttributes.addAttribute("search", search);
        }
        if (roleFilter != null && !roleFilter.isBlank()) {
            redirectAttributes.addAttribute("roleFilter", roleFilter);
        }

        return "redirect:/owner/permissions";
    }
}
