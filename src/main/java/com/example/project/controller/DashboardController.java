package com.example.project.controller;

import com.example.project.constant.RoleConstants;
import com.example.project.context.CurrentUserContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Entry / bridge routes shared by all signed-in users.
 *
 * <p>{@code /} and {@code /dashboard} are not pages of their own: they redirect the signed-in
 * user to their role dashboard (e.g. {@code /cashier/dashboard}). This is the landing target for
 * the post-login success handler's fallback and for the change-password redirect, so role routing
 * stays in one place.</p>
 */
@Controller
public class DashboardController {

    private final CurrentUserContext currentUserContext;

    public DashboardController(CurrentUserContext currentUserContext) {
        this.currentUserContext = currentUserContext;
    }

    @GetMapping({"/", "/dashboard"})
    public String home() {
        String role = currentUserContext.getCurrentRole();
        if (role == null) {
            return "redirect:/signin";
        }
        return "redirect:" + RoleConstants.dashboardPath(role);
    }

    /** Access-denied page shown when an authenticated user lacks the required role. */
    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }

    // --- Legacy scaffold sample pages (kept reachable; not part of the role navigation) ---

    @GetMapping("/inventory")
    public String inventory() {
        return "inventory";
    }

    @GetMapping("/create-product")
    public String createProduct() {
        return "create-product";
    }

    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }

    @GetMapping("/404-error")
    public String notFoundPage() {
        return "404-error";
    }

    @GetMapping("/docs")
    public String docs() {
        return "docs";
    }
}
