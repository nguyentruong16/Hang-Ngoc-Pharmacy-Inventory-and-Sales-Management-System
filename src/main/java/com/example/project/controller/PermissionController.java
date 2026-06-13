package com.example.project.controller;

import com.example.project.constant.RoleConstants;
import com.example.project.service.PermissionMatrixService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Owner-only, read-only Permission Table.
 *
 * <p>Access is enforced by Spring Security ({@code /owner/**} requires {@code ROLE_OWNER} in
 * {@code SecurityConfig}), so this controller carries no manual role check.</p>
 *
 * <p>The matrix shown is derived entirely from {@link PermissionMatrixService} (code/config),
 * because there is no Permission/RolePermission table in the database. The screen therefore has
 * no Save action and never mutates anything.</p>
 *
 * <p>The {@code role} query parameter selects which role's matrix to display (default OWNER) —
 * this is only the table's view selector and is unrelated to the signed-in user's own role.</p>
 */
@Controller
@RequestMapping("/owner/permissions")
public class PermissionController {

    private final PermissionMatrixService permissionMatrixService;

    public PermissionController(PermissionMatrixService permissionMatrixService) {
        this.permissionMatrixService = permissionMatrixService;
    }

    @GetMapping
    public String permissions(@RequestParam(name = "role", required = false) String role, Model model) {
        String selectedRole = RoleConstants.isValid(role) ? role : RoleConstants.OWNER;

        model.addAttribute("selectedRole", selectedRole);
        model.addAttribute("selectedRoleDisplay", RoleConstants.displayName(selectedRole));
        model.addAttribute("roles", RoleConstants.ALL);
        model.addAttribute("actions", PermissionMatrixService.ACTIONS);
        model.addAttribute("rows", permissionMatrixService.buildMatrix(selectedRole));
        return "owner/permissions";
    }
}
