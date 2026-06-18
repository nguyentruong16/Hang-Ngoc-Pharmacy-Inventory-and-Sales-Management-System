package com.example.project.controller;

import com.example.project.constant.RoleConstants;
import com.example.project.service.OwnerPermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Owner-only "Phân quyền theo chi nhánh" (role-by-branch) screen.
 *
 * <p>Manages real {@code AccountPermission} rows — which account holds which role at which
 * branch — via {@link OwnerPermissionService}. Access is enforced by Spring Security
 * ({@code /owner/**} requires {@code ROLE_OWNER}), so no manual role check is needed here.</p>
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
                       @RequestParam(name = "branchId", required = false) Integer branchId,
                       @RequestParam(name = "role", required = false) String role,
                       Model model) {
        model.addAttribute("assignments", ownerPermissionService.listAssignments(search, branchId, role));
        model.addAttribute("accounts", ownerPermissionService.listAccounts());
        model.addAttribute("branches", ownerPermissionService.listBranches());
        model.addAttribute("roleLabels", RoleConstants.vietnameseLabels());
        // echo current filter values back into the form
        model.addAttribute("search", search);
        model.addAttribute("filterBranchId", branchId);
        model.addAttribute("filterRole", role);
        model.addAttribute("pageTitle", "Bảng phân quyền");
        return "owner/permissions";
    }

    @PostMapping
    public String create(@RequestParam(name = "accountId", required = false) Integer accountId,
                         @RequestParam(name = "branchId", required = false) Integer branchId,
                         @RequestParam(name = "role", required = false) String role,
                         RedirectAttributes redirectAttributes) {
        try {
            ownerPermissionService.create(accountId, branchId, role);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm phân quyền thành công");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/owner/permissions";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Integer id,
                         @RequestParam(name = "accountId", required = false) Integer accountId,
                         @RequestParam(name = "branchId", required = false) Integer branchId,
                         @RequestParam(name = "role", required = false) String role,
                         RedirectAttributes redirectAttributes) {
        try {
            ownerPermissionService.update(id, accountId, branchId, role);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phân quyền thành công");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/owner/permissions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            ownerPermissionService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa phân quyền thành công");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/owner/permissions";
    }
}
