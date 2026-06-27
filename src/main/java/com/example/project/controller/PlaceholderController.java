package com.example.project.controller;

import com.example.project.service.SidebarMenuService;
import com.example.project.view.SidebarMenuItem;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves a single generic placeholder page for every role-specific route in the sidebar
 * that does not yet have a full implementation.
 *
 * <p>This exists so the role-based navigation is fully clickable while the real module screens
 * are still being built. The page title / module shown are looked up from {@link SidebarMenuService} by request URI,
 * so labels live in exactly one place (the menu config). Real module pages can later
 * replace these by adding more specific mappings — Spring will prefer the exact path over
 * these placeholder mappings. {@code /owner/permissions} is intentionally excluded (it has
 * its own {@link PermissionController}).</p>
 *
 * <p>Routes are enumerated explicitly (no greedy wildcard) so unknown URLs still 404.</p>
 */
@Controller
public class PlaceholderController {

    private final SidebarMenuService sidebarMenuService;

    public PlaceholderController(SidebarMenuService sidebarMenuService) {
        this.sidebarMenuService = sidebarMenuService;
    }

    @GetMapping({
            "/owner/dashboard",
            "/owner/types",
            "/owner/producers",
            "/owner/suppliers",
            "/owner/debts",
            "/owner/vat-invoices",
            "/owner/financial-setting",
            "/owner/customers",
            "/owner/approvals",
            "/owner/notifications"
    })
    public String owner(HttpServletRequest request, Model model) {
        return render(request, model);
    }

    @GetMapping({
            "/chief-pharmacist/dashboard",
            "/chief-pharmacist/selling",
            "/chief-pharmacist/positions",
            "/chief-pharmacist/origins",
            "/chief-pharmacist/types",
            "/chief-pharmacist/producers",
            "/chief-pharmacist/suppliers",
            "/chief-pharmacist/procurements",
            "/chief-pharmacist/purchase-requisitions",
            "/chief-pharmacist/purchase-invoices",
            "/chief-pharmacist/debts",
            "/chief-pharmacist/invoices",
            "/chief-pharmacist/incomes",
            "/chief-pharmacist/expenses",
            "/chief-pharmacist/returns",
            "/chief-pharmacist/customers",
            "/chief-pharmacist/shift-reports",
            "/chief-pharmacist/notifications"
    })
    public String chiefPharmacist(HttpServletRequest request, Model model) {
        return render(request, model);
    }

    @GetMapping({
            "/pharmacist/dashboard",
            "/pharmacist/selling",
            "/pharmacist/invoices",
            "/pharmacist/customers",
            "/pharmacist/shift-reports",
            "/pharmacist/notifications"
    })
    public String pharmacist(HttpServletRequest request, Model model) {
        return render(request, model);
    }

    @GetMapping({
            "/accountant/dashboard",
            "/accountant/debts",
            "/accountant/expenses",
            "/accountant/daily-reports",
            "/accountant/shift-reports",
            "/accountant/invoices",
            "/accountant/vat-invoices",
            "/accountant/notifications"
    })
    public String accountant(HttpServletRequest request, Model model) {
        return render(request, model);
    }

    private String render(HttpServletRequest request, Model model) {
        SidebarMenuItem item = sidebarMenuService.findByUri(request.getRequestURI());
        model.addAttribute("pageTitle", item != null ? item.getLabel() : "Trang");
        model.addAttribute("moduleName", item != null ? item.getModule() : "-");
        return "placeholder";
    }
}
