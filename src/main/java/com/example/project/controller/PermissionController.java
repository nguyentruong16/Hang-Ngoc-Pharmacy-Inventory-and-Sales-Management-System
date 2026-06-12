package com.example.project.controller;

import com.example.project.constant.RoleConstants;
import com.example.project.context.CurrentUserContext;
import com.example.project.service.PermissionMatrixService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Owner-only, read-only Permission Table.
 *
 * <p>The matrix shown is derived entirely from {@link PermissionMatrixService} (code/config),
 * because there is no Permission/RolePermission table in the database. The screen therefore
 * has no Save action and never mutates anything.</p>
 *
 * <p>The {@code role} query parameter selects which role's matrix to display (default OWNER)
 * — it is unrelated to the current user's own role, which comes from the session.</p>
 */
@Controller
@RequestMapping("/owner/permissions")
public class PermissionController {

    private final PermissionMatrixService permissionMatrixService;
    private final CurrentUserContext currentUserContext;

    public PermissionController(PermissionMatrixService permissionMatrixService,
                                CurrentUserContext currentUserContext) {
        this.permissionMatrixService = permissionMatrixService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping
    public String permissions(@RequestParam(name = "role", required = false) String role,
                              HttpServletRequest request,
                              Model model) {
        // Minimal route protection using existing session data only: if someone is actively
        // signed in as a non-owner role, send them to their own dashboard. When no role is
        // set yet (login not implemented), access is allowed so the page stays demonstrable.
        if (currentUserContext.hasAuthenticatedRole(request)
                && !RoleConstants.OWNER.equals(currentUserContext.getCurrentRole(request))) {
            return "redirect:" + RoleConstants.dashboardPath(currentUserContext.getCurrentRole(request));
        }

        String selectedRole = RoleConstants.isValid(role) ? role : RoleConstants.OWNER;

        model.addAttribute("selectedRole", selectedRole);
        model.addAttribute("selectedRoleDisplay", RoleConstants.displayName(selectedRole));
        model.addAttribute("roles", RoleConstants.ALL);
        model.addAttribute("actions", PermissionMatrixService.ACTIONS);
        model.addAttribute("rows", permissionMatrixService.buildMatrix(selectedRole));
        return "owner/permissions";
    }
}
